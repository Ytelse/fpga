#ifndef __USB_DEVICE_H_
#define __USB_DEVICE_H_

#include "libusb.h"

/* USB DEVICE DESCRIPTOR FPGA */	
#define VENDOR_ID 	0x4ac3
#define PRODUCT_ID 	0xa200
#define EP_IN		0x81 /* TODO: Get proper endpoint addresses */
#define EP_OUT		0x01

int connect(libusb_context* context, libusb_device_handle** dev_handle, int interface);

#endif