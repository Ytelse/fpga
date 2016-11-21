#include <stdlib.h>
#include <stdio.h>
#include <stdbool.h>

#include <string.h>
#include <assert.h>
#include <time.h>
#include <sys/time.h>

#include "libusb.h"

#include "usb_device.h"

#define UNUSED(x) (void) x

#define INPUT_FILE "bit_mnist_images"

#define TIMEOUT 200

#define NOF_IMAGES 3*100
#define IMG_SIZE 98

#define IMG_PR_TRANSFER 3

unsigned char images[NOF_IMAGES*IMG_SIZE];

bool pending_write;

int main(int argc, char** argv) {
	int rc;

	struct timeval start, end;
	long int ms;
	double s;

	fprintf(stdout, "\x1b[32m========================================================\x1b[0m\n");
	fprintf(stdout, "\x1b[36m                        USB <-> FPGA                    \x1b[0m\n");
	fprintf(stdout, "\x1b[32m========================================================\x1b[0m\n");

	/* Debug output file */
	FILE* f;
	f = fopen(INPUT_FILE, "rb");
	assert(f);

	/* Read images */
	gettimeofday(&start, NULL);
	for (int i = 0; i < NOF_IMAGES; i++) {
		for (int j = 0; j < IMG_SIZE; j++) {
			images[i*IMG_SIZE + j] = getc(f);
		}
	}
	gettimeofday(&end, NULL);
	ms = ((end.tv_sec * 1000000 + end.tv_usec) - (start.tv_sec * 1000000 + start.tv_usec));
	s = ms/1e6;
	printf("File I/O finished in %.3f seconds.\n", s);

	/* libusb variables */
	libusb_context* context = NULL;
	libusb_init(&context);
	libusb_set_debug(context, 3);

	libusb_device_handle* dev_handle = NULL;

	rc = connect(context, &dev_handle, 1);

	if (rc) {
		printf("Connection failed. Exiting...\n");
		return -1;
	}

	int cnt = 0;
	long long tot_xferred = 0;
	gettimeofday(&start, NULL);

	for (int i = 0; i < NOF_IMAGES; i+= IMG_PR_TRANSFER) {
		int xferred;
		libusb_bulk_transfer(dev_handle, EP_OUT, &images[i*IMG_SIZE], IMG_SIZE*IMG_PR_TRANSFER, &xferred, TIMEOUT);
		cnt++;
		tot_xferred += xferred;
		gettimeofday(&end, NULL);
		ms = ((end.tv_sec * 1000000 + end.tv_usec) - (start.tv_sec * 1000000 + start.tv_usec));
		s = ms/1e6;
		fprintf(stdout, "\rSent: \x1b[32m%.3f\x1b[0m MB, duration: \x1b[32m%.3f\x1b[0m, transfer speed: \x1b[32m%.3f\x1b[0m Mbps ", (tot_xferred/1e6), s, (tot_xferred/1e6)/s);
		fflush(stdout);
	}
	fprintf(stdout, "\n");
	fprintf(stdout, "\x1b[32m========================================================\x1b[0m\n");
	fprintf(stdout, "Duration:                            %.3f\n", s);
	fprintf(stdout, "Data transferred:                    %llu B\n", tot_xferred);
	fprintf(stdout, "Avg. transfer speed:                 %.3f Mbps\n", (tot_xferred/1e6)/s);
	fprintf(stdout, "\x1b[32m========================================================\x1b[0m\n");

	fclose(f);

	libusb_release_interface(dev_handle, 0);
	libusb_close(dev_handle);
	libusb_exit(context);
}

