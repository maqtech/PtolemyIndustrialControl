--Ptolemy VHDL Code Generation Core Library
--pttest: Test Block for comparing generated VHDL for bit and cycle
--accuracy w.r.t. Ptolemy model.
--THIS IS A TEST BENCH MODULE AND WILL NOT SYNTHESIZE TO HARDWARE.
--Author: Vinayak Nagpal

library ieee;
use ieee.std_logic_1164.all;
library ieee_proposed;
use ieee_proposed.math_utility_pkg.all;
use ieee_proposed.fixed_pkg.all;
use work.pt_utility.all;

--type CORRECTVALS is array (integer range <>) of real;


entity pttest is
GENERIC(
	LENGTH		:	integer := 3;
	LIST		:	CORRECTVALS(1 to LENGTH) := {0.23,0.345,-1.0};
	INPUT_HIGH	:	integer	:= 0;
	INPUT_LOW	:	integer := -15;
	FIXED_SIGN	:	FIXED_TYPE_SIGN := SIGNED;	
);
PORT (
	clk			:	in 	std_logic ;
	data_in		:	in	std_logic_vector (INPUT_HIGH-INPUT_LOW DOWNTO 0)
);
end pttest;

ARCHITECTURE behave OF pttest IS

SIGNAL In_signed	:	sfixed(INPUT_HIGH DOWNTO INPUT_LOW);	 
SIGNAL In_unsigned	:	ufixed(INPUT_HIGH DOWNTO INPUT_LOW);	 
SIGNAL count		:	integer :=0;	 

BEGIN

In_signed 	<= to_sfixed(data_in,INPUTA_HIGH,INPUTA_LOW);
In_unsigned <= to_ufixed(data_in,INPUTA_HIGH,INPUTA_LOW);

compare : process(clk)
	variable In_real	: real := 0.0;
begin
		if clk'event and clk = '1' then
			if count = LENGTH then
				count <= count;
			else
				count <= count + 1;
				if FIXED_SIGN = SIGNED then
					In_real := to_real(In_signed); 
					assert CORRECTVALS(count)=In_real
					report to_string(CORRECTVALS(count)) & "/=" & to_string(In_real)
					severity error;
				else if FIXED_SIGN = UNSIGNED then
					In_real := to_real(In_unsigned); 
					assert CORRECTVALS(count)=In_real
					report to_string(CORRECTVALS(count)) & "/=" & to_string(In_real)
					severity error;
				end if;	
			end if;
		end if;
end process compare ;
END behave ;

