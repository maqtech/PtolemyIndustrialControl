--Ptolemy VHDL Code Generation Core Library
--pt_ufixed_add2: 	
--2 input adder for unsigned fixed point operands. 
--Latency programmable from 1 to 5.
--Parametrizeable to any size fixed point operation.
--Uses truncate and wrap on overflow.					
--Posedge clock.
--Author: Vinayak Nagpal

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
library ieee_proposed;
use ieee_proposed.math_utility_pkg.all;
use ieee_proposed.fixed_pkg.all;
use work.pt_utility.all;

entity pt_ufixed_add2 is
	generic
	(
		INPUTA_HIGH			:	integer		:= 15;
		INPUTA_LOW			:	integer		:= 0;
		INPUTB_HIGH			:	integer		:= 15;
		INPUTB_LOW			:	integer		:= 0;
		LATENCY				: 	integer		:= 3
	) ;
	port
	(
		clk				: IN std_logic;
		A				: IN std_logic_vector (INPUTA_HIGH-INPUTA_LOW DOWNTO 0) ;	
		B				: IN std_logic_vector (INPUTB_HIGH-INPUTB_LOW DOWNTO 0) ;
		SUM				: OUT std_logic_vector (ufixed_high(INPUTA_HIGH,INPUTA_LOW,'+',INPUTB_HIGH,INPUTB_LOW)-ufixed_low(INPUTA_HIGH,INPUTA_LOW,'+',INPUTB_HIGH,INPUTB_LOW) DOWNTO 0) 
	) ;
end pt_ufixed_add2;


ARCHITECTURE behave OF pt_ufixed_add2 IS
--Constants
constant OUTPUT_HIGH 		: integer 	:= ufixed_high(INPUTA_HIGH,INPUTA_LOW,'+',INPUTB_HIGH,INPUTB_LOW);
constant OUTPUT_LOW	 		: integer 	:= ufixed_low(INPUTA_HIGH,INPUTA_LOW,'+',INPUTB_HIGH,INPUTB_LOW);
--Type Declarations
TYPE DELAYLINE is ARRAY (1 to LATENCY) of ufixed (OUTPUT_HIGH DOWNTO OUTPUT_LOW) ;

--Signal Declarations
SIGNAL delay : DELAYLINE;	 
--Input A
signal As :	ufixed (INPUTA_HIGH DOWNTO INPUTA_LOW);
--Input B
signal Bs :	ufixed (INPUTB_HIGH DOWNTO INPUTB_LOW);


BEGIN
As <= to_ufixed(A,As'high,As'low);
Bs <= to_ufixed(B,Bs'high,Bs'low);

SUM <= 	to_slv(delay(LATENCY));
		
adder : process(clk)
begin
	if clk'event and clk = '1' then
		delay(1) <= As+Bs;
		for i in 1 to LATENCY loop
			delay(i+1)<=delay(i);
		end loop;	
	end if;
end process adder ;
END behave ;
