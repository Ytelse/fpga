module AToN(input clk, input reset,
    output io_a_ready,
    input  io_a_valid,
    input [7:0] io_a_bits,
    input  io_n_ready,
    output io_n_valid,
    output[31:0] io_n_bits
);

  wire[31:0] T0;
  wire[15:0] T1;
  reg [7:0] R2;
  wire[7:0] T19;
  wire[7:0] T3;
  wire T4;
  wire T5;
  wire T6;
  reg [2:0] R7;
  wire[2:0] T20;
  wire[2:0] T8;
  wire[2:0] T9;
  wire T10;
  wire[2:0] T11;
  reg [7:0] R12;
  wire[7:0] T21;
  wire[7:0] T13;
  reg [7:0] R14;
  wire[7:0] T22;
  wire[7:0] T15;
  reg [7:0] R16;
  wire[7:0] T23;
  wire[7:0] T17;
  wire[15:0] T18;

`ifndef SYNTHESIS
// synthesis translate_off
  integer initvar;
  initial begin
    #0.002;
    R2 = {1{$random}};
    R7 = {1{$random}};
    R12 = {1{$random}};
    R14 = {1{$random}};
    R16 = {1{$random}};
  end
// synthesis translate_on
`endif

  assign io_n_bits = T0;
  assign T0 = {T18, T1};
  assign T1 = {R12, R2};
  assign T19 = reset ? 8'h0 : T3;
  assign T3 = T4 ? R12 : R2;
  assign T4 = io_a_valid & T5;
  assign T5 = ~ T6;
  assign T6 = R7 == 3'h4;
  assign T20 = reset ? 3'h0 : T8;
  assign T8 = T4 ? T11 : T9;
  assign T9 = T10 ? 3'h0 : R7;
  assign T10 = io_n_ready & T6;
  assign T11 = R7 + 3'h1;
  assign T21 = reset ? 8'h0 : T13;
  assign T13 = T4 ? R14 : R12;
  assign T22 = reset ? 8'h0 : T15;
  assign T15 = T4 ? R16 : R14;
  assign T23 = reset ? 8'h0 : T17;
  assign T17 = T4 ? io_a_bits : R16;
  assign T18 = {R16, R14};
  assign io_n_valid = T6;
  assign io_a_ready = T5;

  always @(posedge clk) begin
    if(reset) begin
      R2 <= 8'h0;
    end else if(T4) begin
      R2 <= R12;
    end
    if(reset) begin
      R7 <= 3'h0;
    end else if(T4) begin
      R7 <= T11;
    end else if(T10) begin
      R7 <= 3'h0;
    end
    if(reset) begin
      R12 <= 8'h0;
    end else if(T4) begin
      R12 <= R14;
    end
    if(reset) begin
      R14 <= 8'h0;
    end else if(T4) begin
      R14 <= R16;
    end
    if(reset) begin
      R16 <= 8'h0;
    end else if(T4) begin
      R16 <= io_a_bits;
    end
  end
endmodule

module AsyncFifo(input ulpi_clk, input clk, input reset,
    output io_enq_ready,
    input  io_enq_valid,
    input [7:0] io_enq_bits,
    input  io_deq_ready,
    output io_deq_valid,
    output[7:0] io_deq_bits
    //output[4:0] io_count
);

  wire[7:0] T1;
  reg [7:0] mem [15:0];
  wire[7:0] T2;
  wire T3;
  wire[3:0] T4;
  reg [4:0] wptr_bin;
  wire[4:0] T20;
  wire[4:0] wptr_bin_next;
  wire[4:0] T21;
  wire T5;
  reg  not_full;
  wire T22;
  wire T6;
  wire T7;
  reg  s2_rst_deq;
  wire T23;
  reg  s1_rst_deq;
  wire T24;
  wire not_full_next;
  wire T8;
  wire[4:0] T9;
  wire[2:0] T10;
  reg [4:0] s2_rptr_gray;
  wire[4:0] T25;
  reg [4:0] s1_rptr_gray;
  wire[4:0] T26;
  reg [4:0] rptr_gray;
  wire[4:0] T27;
  wire[4:0] rptr_gray_next;
  wire[4:0] rptr_bin_next;
  wire[4:0] T28;
  wire T11;
  reg  not_empty;
  wire T29;
  wire T12;
  wire T13;
  reg  s2_rst_enq;
  wire T30;
  reg  s1_rst_enq;
  wire T31;
  wire not_empty_next;
  wire T14;
  reg [4:0] s2_wptr_gray;
  wire[4:0] T32;
  reg [4:0] s1_wptr_gray;
  wire[4:0] T33;
  reg [4:0] wptr_gray;
  wire[4:0] T34;
  reg [4:0] rptr_bin;
  wire[4:0] T35;
  wire[4:0] T36;
  wire[3:0] T15;
  wire[1:0] T16;
  wire[1:0] T17;
  wire[4:0] wptr_gray_next;
  wire[4:0] T37;
  wire[3:0] T18;
  wire[3:0] T19;

`ifndef SYNTHESIS
// synthesis translate_off
  integer initvar;
  initial begin
    #0.002;
    for (initvar = 0; initvar < 16; initvar = initvar+1)
      mem[initvar] = {1{$random}};
    wptr_bin = {1{$random}};
    not_full = {1{$random}};
    s2_rst_deq = {1{$random}};
    s1_rst_deq = {1{$random}};
    s2_rptr_gray = {1{$random}};
    s1_rptr_gray = {1{$random}};
    rptr_gray = {1{$random}};
    not_empty = {1{$random}};
    s2_rst_enq = {1{$random}};
    s1_rst_enq = {1{$random}};
    s2_wptr_gray = {1{$random}};
    s1_wptr_gray = {1{$random}};
    wptr_gray = {1{$random}};
    rptr_bin = {1{$random}};
  end
// synthesis translate_on
`endif

`ifndef SYNTHESIS
// synthesis translate_off
//  assign io_count = {1{$random}};
// synthesis translate_on
`endif
  assign io_deq_bits = T1;
  assign T1 = mem[T19];
  assign T3 = io_enq_valid & io_enq_ready;
  assign T4 = wptr_bin[3:0];
  assign T20 = reset ? 5'h0 : wptr_bin_next;
  assign wptr_bin_next = wptr_bin + T21;
  assign T21 = {4'h0, T5};
  assign T5 = io_enq_valid & not_full;
  assign T22 = reset ? 1'h0 : T6;
  assign T6 = not_full_next & T7;
  assign T7 = s2_rst_deq ^ 1'h1;
  assign T23 = reset ? 1'h0 : s1_rst_deq;
  assign T24 = reset ? 1'h0 : reset;
  assign not_full_next = T8 ^ 1'h1;
  assign T8 = wptr_gray_next == T9;
  assign T9 = {T16, T10};
  assign T10 = s2_rptr_gray[2:0];
  assign T25 = reset ? 5'h0 : s1_rptr_gray;
  assign T26 = reset ? 5'h0 : rptr_gray;
  assign T27 = reset ? 5'h0 : rptr_gray_next;
  assign rptr_gray_next = T36 ^ rptr_bin_next;
  assign rptr_bin_next = rptr_bin + T28;
  assign T28 = {4'h0, T11};
  assign T11 = io_deq_ready & not_empty;
  assign T29 = reset ? 1'h0 : T12;
  assign T12 = not_empty_next & T13;
  assign T13 = s2_rst_enq ^ 1'h1;
  assign T30 = reset ? 1'h0 : s1_rst_enq;
  assign T31 = reset ? 1'h0 : reset;
  assign not_empty_next = T14 ^ 1'h1;
  assign T14 = rptr_gray_next == s2_wptr_gray;
  assign T32 = reset ? 5'h0 : s1_wptr_gray;
  assign T33 = reset ? 5'h0 : wptr_gray;
  assign T34 = reset ? 5'h0 : wptr_gray_next;
  assign T35 = reset ? 5'h0 : rptr_bin_next;
  assign T36 = {1'h0, T15};
  assign T15 = rptr_bin_next >> 1'h1;
  assign T16 = ~ T17;
  assign T17 = s2_rptr_gray[4:3];
  assign wptr_gray_next = T37 ^ wptr_bin_next;
  assign T37 = {1'h0, T18};
  assign T18 = wptr_bin_next >> 1'h1;
  assign T19 = rptr_bin[3:0];
  assign io_deq_valid = not_empty;
  assign io_enq_ready = not_full;

  always @(posedge ulpi_clk) begin
    if (T3)
      mem[T4] <= io_enq_bits;
    if(reset) begin
      wptr_bin <= 5'h0;
    end else begin
      wptr_bin <= wptr_bin_next;
    end
    if(reset) begin
      not_full <= 1'h0;
    end else begin
      not_full <= T6;
    end
    if(reset) begin
      s2_rst_deq <= 1'h0;
    end else begin
      s2_rst_deq <= s1_rst_deq;
    end
    if(reset) begin
      s1_rst_deq <= 1'h0;
    end else begin
      s1_rst_deq <= reset;
    end
    if(reset) begin
      s2_rptr_gray <= 5'h0;
    end else begin
      s2_rptr_gray <= s1_rptr_gray;
    end
    if(reset) begin
      s1_rptr_gray <= 5'h0;
    end else begin
      s1_rptr_gray <= rptr_gray;
    end
    if(reset) begin
      wptr_gray <= 5'h0;
    end else begin
      wptr_gray <= wptr_gray_next;
    end
  end
  always @(posedge clk) begin
    if(reset) begin
      rptr_gray <= 5'h0;
    end else begin
      rptr_gray <= rptr_gray_next;
    end
    if(reset) begin
      not_empty <= 1'h0;
    end else begin
      not_empty <= T12;
    end
    if(reset) begin
      s2_rst_enq <= 1'h0;
    end else begin
      s2_rst_enq <= s1_rst_enq;
    end
    if(reset) begin
      s1_rst_enq <= 1'h0;
    end else begin
      s1_rst_enq <= reset;
    end
    if(reset) begin
      s2_wptr_gray <= 5'h0;
    end else begin
      s2_wptr_gray <= s1_wptr_gray;
    end
    if(reset) begin
      s1_wptr_gray <= 5'h0;
    end else begin
      s1_wptr_gray <= wptr_gray;
    end
    if(reset) begin
      rptr_bin <= 5'h0;
    end else begin
      rptr_bin <= rptr_bin_next;
    end
  end
endmodule

module ClockDomainBridge(input clk, input ulpi_clk, input reset,
    output io_usb_data_ready,
    input  io_usb_data_valid,
    input [7:0] io_usb_data_bits,
    input  io_net_data_ready,
    output io_net_data_valid,
    output[31:0] io_net_data_bits
);

  wire AToN_inst_io_a_ready;
  wire AToN_inst_io_n_valid;
  wire[31:0] AToN_inst_io_n_bits;
  wire AsyncFifo_inst_io_enq_ready;
  wire AsyncFifo_inst_io_deq_valid;
  wire[7:0] AsyncFifo_inst_io_deq_bits;


  assign io_net_data_bits = AToN_inst_io_n_bits;
  assign io_net_data_valid = AToN_inst_io_n_valid;
  assign io_usb_data_ready = AsyncFifo_inst_io_enq_ready;
  AToN AToN_inst(.clk(clk), .reset(reset),
       .io_a_ready( AToN_inst_io_a_ready ),
       .io_a_valid( AsyncFifo_inst_io_deq_valid ),
       .io_a_bits( AsyncFifo_inst_io_deq_bits ),
       .io_n_ready( io_net_data_ready ),
       .io_n_valid( AToN_inst_io_n_valid ),
       .io_n_bits( AToN_inst_io_n_bits )
  );
  AsyncFifo AsyncFifo_inst(.ulpi_clk(ulpi_clk), .clk(clk), .reset(reset),
       .io_enq_ready( AsyncFifo_inst_io_enq_ready ),
       .io_enq_valid( io_usb_data_valid ),
       .io_enq_bits( io_usb_data_bits ),
       .io_deq_ready( AToN_inst_io_a_ready ),
       .io_deq_valid( AsyncFifo_inst_io_deq_valid ),
       .io_deq_bits( AsyncFifo_inst_io_deq_bits )
       //.io_count(  )
  );
endmodule

