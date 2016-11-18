module AToN(input clk, input reset,
    output io_a_ready,
    input  io_a_valid,
    input [7:0] io_a_bits,
    input  io_n_ready,
    output io_n_valid,
    output[71:0] io_n_bits
);

  wire[71:0] T0;
  wire[39:0] T1;
  wire[23:0] T2;
  wire[15:0] T3;
  reg [7:0] R4;
  wire[7:0] T34;
  wire[7:0] T5;
  wire T6;
  wire T7;
  wire T8;
  reg [3:0] R9;
  wire[3:0] T35;
  wire[3:0] T10;
  wire[3:0] T11;
  wire T12;
  wire[3:0] T13;
  reg [7:0] R14;
  wire[7:0] T36;
  wire[7:0] T15;
  reg [7:0] R16;
  wire[7:0] T37;
  wire[7:0] T17;
  reg [7:0] R18;
  wire[7:0] T38;
  wire[7:0] T19;
  reg [7:0] R20;
  wire[7:0] T39;
  wire[7:0] T21;
  reg [7:0] R22;
  wire[7:0] T40;
  wire[7:0] T23;
  reg [7:0] R24;
  wire[7:0] T41;
  wire[7:0] T25;
  reg [7:0] R26;
  wire[7:0] T42;
  wire[7:0] T27;
  reg [7:0] R28;
  wire[7:0] T43;
  wire[7:0] T29;
  wire[15:0] T30;
  wire[31:0] T31;
  wire[15:0] T32;
  wire[15:0] T33;

`ifndef SYNTHESIS
// synthesis translate_off
  integer initvar;
  initial begin
    #0.002;
    R4 = {1{$random}};
    R9 = {1{$random}};
    R14 = {1{$random}};
    R16 = {1{$random}};
    R18 = {1{$random}};
    R20 = {1{$random}};
    R22 = {1{$random}};
    R24 = {1{$random}};
    R26 = {1{$random}};
    R28 = {1{$random}};
  end
// synthesis translate_on
`endif

  assign io_n_bits = T0;
  assign T0 = {T31, T1};
  assign T1 = {T30, T2};
  assign T2 = {R16, T3};
  assign T3 = {R14, R4};
  assign T34 = reset ? 8'h0 : T5;
  assign T5 = T6 ? R14 : R4;
  assign T6 = io_a_valid & T7;
  assign T7 = ~ T8;
  assign T8 = R9 == 4'h9;
  assign T35 = reset ? 4'h0 : T10;
  assign T10 = T6 ? T13 : T11;
  assign T11 = T12 ? 4'h0 : R9;
  assign T12 = io_n_ready & T8;
  assign T13 = R9 + 4'h1;
  assign T36 = reset ? 8'h0 : T15;
  assign T15 = T6 ? R16 : R14;
  assign T37 = reset ? 8'h0 : T17;
  assign T17 = T6 ? R18 : R16;
  assign T38 = reset ? 8'h0 : T19;
  assign T19 = T6 ? R20 : R18;
  assign T39 = reset ? 8'h0 : T21;
  assign T21 = T6 ? R22 : R20;
  assign T40 = reset ? 8'h0 : T23;
  assign T23 = T6 ? R24 : R22;
  assign T41 = reset ? 8'h0 : T25;
  assign T25 = T6 ? R26 : R24;
  assign T42 = reset ? 8'h0 : T27;
  assign T27 = T6 ? R28 : R26;
  assign T43 = reset ? 8'h0 : T29;
  assign T29 = T6 ? io_a_bits : R28;
  assign T30 = {R20, R18};
  assign T31 = {T33, T32};
  assign T32 = {R24, R22};
  assign T33 = {R28, R26};
  assign io_n_valid = T8;
  assign io_a_ready = T7;

  always @(posedge clk) begin
    if(reset) begin
      R4 <= 8'h0;
    end else if(T6) begin
      R4 <= R14;
    end
    if(reset) begin
      R9 <= 4'h0;
    end else if(T6) begin
      R9 <= T13;
    end else if(T12) begin
      R9 <= 4'h0;
    end
    if(reset) begin
      R14 <= 8'h0;
    end else if(T6) begin
      R14 <= R16;
    end
    if(reset) begin
      R16 <= 8'h0;
    end else if(T6) begin
      R16 <= R18;
    end
    if(reset) begin
      R18 <= 8'h0;
    end else if(T6) begin
      R18 <= R20;
    end
    if(reset) begin
      R20 <= 8'h0;
    end else if(T6) begin
      R20 <= R22;
    end
    if(reset) begin
      R22 <= 8'h0;
    end else if(T6) begin
      R22 <= R24;
    end
    if(reset) begin
      R24 <= 8'h0;
    end else if(T6) begin
      R24 <= R26;
    end
    if(reset) begin
      R26 <= 8'h0;
    end else if(T6) begin
      R26 <= R28;
    end
    if(reset) begin
      R28 <= 8'h0;
    end else if(T6) begin
      R28 <= io_a_bits;
    end
  end
endmodule

