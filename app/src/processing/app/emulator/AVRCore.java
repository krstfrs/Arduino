package processing.app.emulator;

public class AVRCore {

	private final static int X = 26;
	private final static int Y = 28;
	private final static int Z = 30;

	private int ip;
	private int[] r = new int[32];

	private boolean i = false;
	private boolean t = false;
	private boolean h = false;
	private boolean s = false;
	private boolean v = false;
	private boolean n = false;
	private boolean z = false;
	private boolean c = false;

	private byte[] progMem;
	private byte[] dataMem;
	private IAddressSpace ioRegisters;

	public AVRCore(byte[] progMem, byte[] dataMem, IAddressSpace ioRegisters) {

		this.progMem = progMem;
		this.dataMem = dataMem;
		this.ioRegisters = ioRegisters;

	}

	public void emulate(int cycles) {

		int c = 0;

		while (c < cycles)
			c += emulateInstruction();

	}

	private int emulateInstruction() {

		int instruction = (progMem[ip] << 8) & progMem[ip + 1];

		// NOP

		if (instruction == 0x0000)
			return 1;

		/*
		 * Arithmetic instructions
		 */

		// 2-operand instructions
		// 0000 01rd dddd rrrr
		// 0000 1xrd dddd rrrr
		// 0001 xxrd dddd rrrr
		// 0010 xxrd dddd rrrr
		// 1001 11rd dddd rrrr (MUL)

		if ((instruction & 0xFC00) == 0x0400
				|| (instruction & 0xF800) == 0x0800
				|| (instruction & 0xF000) == 0x1000
				|| (instruction & 0xF000) == 0x2000) {

			int rd = (instruction & 0x01F0) >>> 4;
			int rr = ((instruction & 0x0200) >>> 5) & (instruction & 0x000F);

			int maskedInstruction = instruction & 0xFC00;

			switch (maskedInstruction) {

			case 0x1C00:
				return instructionAdc(rd, rr);
			case 0x0C00:
				return instructionAdd(rd, rr);
			case 0x2000:
				return instructionAnd(rd, rr);
			case 0x1400:
				return instructionCp(rd, rr);
			case 0x0400:
				return instructionCpc(rd, rr);
			case 0x1000:
				return instructionCpse(rd, rr);
			case 0x2400:
				return instructionEor(rd, rr);
			case 0x2C00:
				return instructionMov(rd, rr);
			case 0x9C00:
				return instructionMul(rd, rr);
			case 0x2800:
				return instructionOr(rd, rr);
			case 0x0800:
				return instructionSbc(rd, rr);
			case 0x1800:
				return instructionSub(rd, rr);

			}

		}

		// 1-operand instructions
		// 1001 010d dddd 0xxx
		// 1001 010d dddd 1010 (DEC)

		if ((instruction & 0xFE08) == 0x9400
				|| (instruction & 0xFE0F) == 0x940A) {

			int rd = (instruction & 0x01F0) >>> 4;

			int maskedInstruction = instruction & 0xFE08;

			switch (maskedInstruction) {

			case 0x9405:
				return instructionAsr(rd);
			case 0x9400:
				return instructionCom(rd);
			case 0x940A:
				return instructionDec(rd);
			case 0x9403:
				return instructionInc(rd);
			case 0x9406:
				return instructionLsr(rd);
			case 0x9401:
				return instructionNeg(rd);
			case 0x9407:
				return instructionRor(rd);
			case 0x9402:
				return instructionSwap(rd);

			}

		}

		// Register-immediate instructions
		// 0011 KKKK hhhh KKKK
		// 01xx KKKK hhhh KKKK
		// 1110 KKKK hhhh KKKK (LDI)

		if ((instruction & 0xF000) == 0x3000
				|| (instruction & 0xC000) == 0x8000) {

			int rd = ((instruction & 0x00F0) >>> 4) + 0x10;
			int k = ((instruction & 0x0F00) >>> 4) & (instruction & 0x000F);

			int maskedInstruction = instruction & 0xF000;

			switch (maskedInstruction) {

			case 0x7000:
				return instructionAndi(rd, k);
			case 0x3000:
				return instructionCpi(rd, k);
			case 0x6000:
				return instructionOri(rd, k);
			case 0x4000:
				return instructionSbci(rd, k);
			case 0x9000:
				return instructionSubi(rd, k);
			case 0xE000:
				return instructionLdi(rd, k);

			}

		}

		// ADIW/SBIW
		// 1001 011x kkpp kkkk

		if ((instruction & 0xFE00) == 0x9600) {

			int rdp = (((instruction & 0x0030) >>> 4) << 1) + 24;
			int k = ((instruction & 0x00C0) >>> 2) & (instruction & 0x000F);

			int maskedInstruction = instruction & 0xFE00;

			switch (maskedInstruction) {

			case 0x9600:
				instructionAdiw(rdp, k);
			case 0x9700:
				instructionSbiw(rdp, k);

			}

		}

		// MOVW
		// 0000 0001 dddd rrrr

		if ((instruction & 0xFF00) == 0x0100) {

			int rd = ((instruction & 0x00F0) >>> 8) << 1;
			int rr = (instruction & 0x000F) << 1;

			return instructionMovw(rd, rr);

		}

		// MULS
		// 0000 0010 dddd rrrr

		if ((instruction & 0xFF00) == 0x0200) {

			int rd = ((instruction & 0x00F0) >>> 8) + 0x10;
			int rr = (instruction & 0x000F) + 0x10;

			return instructionMuls(rd, rr);

		}

		// Other multiply instructions
		// 0000 0011 xddd xrrr

		if ((instruction & 0xFF00) == 0x0300) {

			int rd = ((instruction & 0x0070) >>> 8) + 0x10;
			int rr = (instruction & 0x0007) + 0x10;

			int maskedInstruction = instruction & 0xFF88;

			switch (maskedInstruction) {

			case 0x0308:
				return instructionFmul(rd, rr);
			case 0x0380:
				return instructionFmuls(rd, rr);
			case 0x0388:
				return instructionFmulsu(rd, rr);
			case 0x0300:
				return instructionMulsu(rd, rr);

			}

		}

		/*
		 * Bit operations
		 */

		// SREG bit operations
		// 1001 0100 xbbb 0000

		if ((instruction & 0xFF0F) == 0x9408) {

			int b = (instruction & 0x0070) >> 4;

			int maskedInstruction = instruction & 0xFF8F;

			switch (maskedInstruction) {

			case 0x9418:
				return instructionBclr(b);
			case 0x9408:
				return instructionBset(b);

			}

		}

		// T bit operations
		// 1111 10xd dddd 0bbb

		if ((instruction & 0xFC08) == 0xF800) {

			int rd = (instruction & 0x01F0) >>> 4;
			int b = instruction & 0x0007;

			int maskedInstruction = instruction & 0xFE08;

			switch (maskedInstruction) {

			case 0xF800:
				return instructionBld(rd, b);
			case 0xFA00:
				return instructionBst(rd, b);

			}

		}

		/*
		 * Transfer operations
		 */

		// LD/ST
		// 1001 00xd dddd xxxx

		if ((instruction & 0xFC00) == 0x9000) {

			int rd = (instruction & 0x01F0) >>> 4;

			int maskedInstruction = instruction & 0xFC0F;

			switch (maskedInstruction) {

			case 0x900C:
				return instructionLdX(rd);
			case 0x900D:
				return instructionLdXInc(rd);
			case 0x900E:
				return instructionLdDecX(rd);
			case 0x9009:
				return instructionLdYInc(rd);
			case 0x900A:
				return instructionLdDecY(rd);
			case 0x9001:
				return instructionLdZInc(rd);
			case 0x9002:
				return instructionLdDecZ(rd);
			case 0x920C:
				return instructionStX(rd);
			case 0x920D:
				return instructionStXInc(rd);
			case 0x920E:
				return instructionStDecX(rd);
			case 0x9209:
				return instructionStYInc(rd);
			case 0x920A:
				return instructionStDecY(rd);
			case 0x9201:
				return instructionStZInc(rd);
			case 0x9202:
				return instructionStDecZ(rd);

			}

		}

		// LDD/STD
		// 10k0 kkxd dddd xkkk

		if ((instruction & 0xD000) == 0x8000) {

			int rd = (instruction & 0x01F0) >>> 4;
			int k = ((instruction & 0x2000) >>> 8)
					& ((instruction & 0x0C00) >>> 7) & (instruction & 0x0007);

			int maskedInstruction = instruction & 0xD208;

			switch (maskedInstruction) {

			case 0x8008:
				return instructionLddY(rd, k);
			case 0x8000:
				return instructionLddZ(rd, k);
			case 0x8208:
				return instructionStdY(rd, k);
			case 0x8200:
				return instructionStdZ(rd, k);

			}

		}

		// LPM
		// 1001 000d dddd 010x

		if ((instruction & 0xFE0E) == 0x9004) {

			int rd = (instruction & 0x01F0) >>> 4;

			int maskedInstruction = instruction & 0xFE0F;

			switch (maskedInstruction) {

			case 0x9004:
				return instructionLpmZ(rd);
			case 0x9005:
				return instructionLpmZInc(rd);

			}

		}

		/*
		 * Misc instructions
		 */

		switch (instruction) {

		case 0x95C8:
			return instructionLpmR0();

		}

		throw new OpcodeNotImplementedException();

	}

	private byte dataReadByte(int address) {

		if (address < 0x20)
			return (byte) r[address];

		if (address == 0x5F) {
			return (byte) ((i ? 0x80 : 0) & (t ? 0x40 : 0) & (h ? 0x20 : 0)
					& (s ? 0x10 : 0) & (v ? 0x08 : 0) & (n ? 0x40 : 0)
					& (z ? 0x20 : 0) & (c ? 0x10 : 0));
		}

		if (address < 0x60) {

			return ioRegisters.readByte(address - 0x20);
		}

		return dataMem[address];

	}

	private void dataWriteByte(int address, byte data) {

		if (address < 0x20)
			r[address] = data;

		if (address == 0x5F) {

			i = (data & 0x80) != 0;
			t = (data & 0x40) != 0;
			h = (data & 0x20) != 0;
			s = (data & 0x10) != 0;
			v = (data & 0x08) != 0;
			n = (data & 0x04) != 0;
			z = (data & 0x02) != 0;
			c = (data & 0x01) != 0;

			return;

		}

		if (address < 0x60) {

			ioRegisters.writeByte(address - 0x20, data);

		}

		dataMem[address] = data;

	}

	/*
	 * The implemented instructions are grouped by type, however instructions
	 * not yet implemented are recorded here alphabetically.
	 */

	// TODO: Implement BRBC
	// TODO: Implement BRBS
	// TODO: Implement BRCC
	// TODO: Implement BRCS
	// TODO: Implement BREAK
	// TODO: Implement BREQ
	// TODO: Implement BRGE
	// TODO: Implement BRHC
	// TODO: Implement BRHS
	// TODO: Implement BRID
	// TODO: Implement BRIE
	// TODO: Implement BRLO
	// TODO: Implement BRLT
	// TODO: Implement BRSH
	// TODO: Implement BRTC
	// TODO: Implement BRTS
	// TODO: Implement BRVC
	// TODO: Implement CALL
	// TODO: Implement CBI
	// TODO: Implement CBR
	// TODO: Implement CLR
	// TODO: Implement CPSE
	// TODO: Implement DES
	// TODO: Implement EICALL
	// TODO: Implement EIJMP
	// TODO: Implement ELMP
	// TODO: Implement ICALL
	// TODO: Implement IJMP
	// TODO: Implement IN
	// TODO: Implement JMP
	// TODO: Implement LAC
	// TODO: Implement LAS
	// TODO: Implement LAT
	// TODO: Implement LDS
	// TODO: Implement OUT
	// TODO: Implement POP
	// TODO: Implement PUSH
	// TODO: Implement RCALL
	// TODO: Implement RET
	// TODO: Implement RETI
	// TODO: Implement RJMP
	// TODO: Implement SBI
	// TODO: Implement SBIC
	// TODO: Implement SBIS
	// TODO: Implement SBR
	// TODO: Implement SBRC
	// TODO: Implement SBRS
	// TODO: Implement SER
	// TODO: Implement SLEEP
	// TODO: Implement SPM
	// TODO: Implement STS
	// TODO: Implement WDR
	// TODO: Implement XCH

	/*
	 * Arithmetic instructions
	 */

	private int instructionMov(int rd, int rr) {

		r[rd] = r[rr];

		return 1;

	}

	private int instructionAnd(int rd, int rr) {

		return instructionAndi(rd, r[rr]);

	}

	private int instructionAndi(int rd, int k) {

		int res = k & r[rd];

		v = false;
		z = (res == 0);
		n = (res & 0x80) != 0;
		s = n ^ v;

		r[rd] = res;

		return 1;

	}

	private int instructionOr(int rd, int rr) {

		return instructionOri(rd, r[rr]);

	}

	private int instructionOri(int rd, int k) {

		int res = k | r[rd];

		v = false;
		z = (res == 0);
		n = (res & 0x80) != 0;
		s = n ^ v;

		r[rd] = res;

		return 1;

	}

	private int instructionEor(int rd, int rr) {

		int res = r[rr] ^ r[rd];

		v = false;
		z = (res == 0);
		n = (res & 0x80) != 0;
		s = n ^ v;

		r[rd] = res;

		return 1;

	}

	private int instructionAdc(int rd, int rr) {

		return instructionHelperAdd(rr, rd, true);

	}

	private int instructionAdd(int rd, int rr) {

		return instructionHelperAdd(rr, rd, false);

	}

	private int instructionHelperAdd(int rd, int rr, boolean carry) {

		int cin = carry ? (c ? 1 : 0) : 0;

		int res = (r[rr] + r[rd] + cin) & 0xFF;

		boolean rr3 = (r[rr] & 0x08) != 0;
		boolean rd3 = (r[rd] & 0x08) != 0;
		boolean res3 = (res & 0x08) != 0;

		boolean rr7 = (r[rr] & 0x80) != 0;
		boolean rd7 = (r[rd] & 0x80) != 0;
		boolean res7 = (res & 0x80) != 0;

		h = (rd3 && rr3) || (rr3 && !res3) || (!res3 && rd3);
		c = (rd7 && rr7) || (rr7 && !res7) || (!res7 && rd7);
		v = (rd7 && rr7 && !res7) || (!rd7 && !rr7 && res7);
		z = (res == 0);
		n = res7;
		s = n ^ v;

		r[rd] = res;

		return 1;

	}

	private int instructionCp(int rd, int rr) {

		return instructionHelperSubi(rd, r[rr], false, false);

	}

	private int instructionCpi(int rd, int k) {

		return instructionHelperSubi(rd, k, false, false);

	}

	private int instructionCpc(int rd, int rr) {

		return instructionHelperSubi(rd, r[rr], false, true);

	}

	private int instructionSbc(int rd, int rr) {

		return instructionHelperSubi(rd, r[rr], true, true);

	}

	private int instructionSbci(int rd, int k) {

		return instructionHelperSubi(rd, k, true, true);

	}

	private int instructionSub(int rd, int rr) {

		return instructionHelperSubi(rd, r[rr], true, false);

	}

	private int instructionSubi(int rd, int k) {

		return instructionHelperSubi(rd, k, true, false);

	}

	private int instructionHelperSubi(int rd, int k, boolean save, boolean carry) {

		int cin = carry ? (c ? 1 : 0) : 0;

		int res = (r[rd] - k - cin) & 0xFF;

		boolean k3 = (k & 0x08) != 0;
		boolean rd3 = (r[rd] & 0x08) != 0;
		boolean res3 = (res & 0x08) != 0;

		boolean k7 = (k & 0x80) != 0;
		boolean rd7 = (r[rd] & 0x80) != 0;
		boolean res7 = (res & 0x80) != 0;

		h = (!rd3 && k3) || (k3 && res3) || (res3 && !rd3);
		c = (!rd7 && k7) || (k7 && res7) || (res7 && !rd7);
		v = (rd7 && !k7 && !res7) || (!rd7 && k7 && res7);
		z = (res == 0);
		n = res7;
		s = n ^ v;

		if (save)
			r[rd] = res;

		return 1;

	}

	private int instructionLdi(int rd, int k) {

		r[rd] = k;

		return 1;

	}

	private int instructionCpse(int rd, int rr) {

		throw new OpcodeNotImplementedException();

	}

	private int instructionAsr(int rd) {

		return instructionHelperShr(rd, true, false);

	}

	private int instructionLsr(int rd) {

		return instructionHelperShr(rd, false, false);

	}

	private int instructionRor(int rd) {

		return instructionHelperShr(rd, false, true);

	}

	private int instructionHelperShr(int rd, boolean arithmetic, boolean rotate) {

		int res = r[rd] >>> 1;

		if (arithmetic)
			res &= (r[rd] & 0x80);

		if (rotate)
			res &= c ? 0x80 : 0;

		c = (r[rd] & 0x01) != 0;
		z = (res == 0);
		n = (res & 0x80) != 0;
		v = n ^ c;
		s = n ^ v;

		r[rd] = res;

		return 1;

	}

	private int instructionCom(int rd) {

		int res = (0xFF - r[rd]) & 0xFF;

		c = true;
		z = (res == 0);
		v = false;
		n = (res & 0x80) != 0;
		s = n ^ v;

		r[rd] = res;

		return 1;

	}

	private int instructionNeg(int rd) {

		int res = (0x00 - r[rd]) & 0xFF;

		boolean rd3 = (r[rd] & 0x08) != 0;
		boolean res3 = (res & 0x08) != 0;

		h = rd3 & res3;
		c = (res != 0);
		z = (res == 0);
		v = (res == 0x80);
		n = (res & 0x80) != 0;
		s = n ^ v;

		return 1;

	}

	private int instructionDec(int rd) {

		int res = r[rd] - 1;

		z = (res == 0);
		n = (res & 0x80) != 0;
		v = (res == 0x7F);
		s = n ^ v;

		r[rd] = res;

		return 1;
	}

	private int instructionInc(int rd) {

		int res = r[rd] + 1;

		z = (res == 0);
		n = (res & 0x80) != 0;
		v = (res == 0x80);
		s = n ^ v;

		r[rd] = res;

		return 1;

	}

	private int instructionSwap(int rd) {

		r[rd] = (r[rd] >>> 4) & (r[rd] << 4);

		return 1;

	}

	private int instructionAdiw(int rdp, int k) {

		int rrp = r[rdp] & (r[rdp + 1] << 8);
		int res = rrp + k;

		boolean rrp15 = (res & 0x8000) != 0;
		boolean res15 = (res & 0x8000) != 0;

		c = (!res15) && rrp15;
		z = (res == 0);
		n = res15;
		v = (!rrp15) && res15;
		s = n ^ v;

		r[rdp] = res & 0xFF;
		r[rdp + 1] = (res & 0xFF00) >>> 8;

		return 2;

	}

	private int instructionSbiw(int rdp, int k) {

		int rrp = r[rdp] & (r[rdp + 1] << 8);
		int res = rrp - k;

		boolean rrp15 = (res & 0x8000) != 0;
		boolean res15 = (res & 0x8000) != 0;

		c = res15 && (!rrp15);
		z = (res == 0);
		n = res15;
		v = rrp15 && (!res15);
		s = n ^ v;

		r[rdp] = res & 0xFF;
		r[rdp + 1] = (res & 0xFF00) >>> 8;

		return 2;

	}

	private int instructionMovw(int rd, int rr) {

		r[rd] = r[rr];
		r[rd + 1] = r[rr + 1];

		return 1;

	}

	private int instructionFmul(int rd, int rr) {

		return instructionHelperMul(rd, rr, false, false, true);

	}

	private int instructionFmuls(int rd, int rr) {

		return instructionHelperMul(rd, rr, true, true, true);

	}

	private int instructionFmulsu(int rd, int rr) {

		return instructionHelperMul(rd, rr, true, false, true);

	}

	private int instructionMul(int rd, int rr) {

		return instructionHelperMul(rd, rr, false, false, false);

	}

	private int instructionMuls(int rd, int rr) {

		return instructionHelperMul(rd, rr, true, true, false);

	}

	private int instructionMulsu(int rd, int rr) {

		return instructionHelperMul(rd, rr, true, false, false);

	}

	private int instructionHelperMul(int rd, int rr, boolean rdsigned,
			boolean rrsigned, boolean shift) {

		int rrd = r[rd];
		int rrr = r[rr];

		if (rdsigned && (rrd & 0x80) != 0)
			rrd &= 0xFFFFFF00;

		if (rrsigned && (rrr & 0x80) != 0)
			rrr &= 0xFFFFFF00;

		int res = (rrd * rrr) & 0xFFFF;

		c = (res & 0x8000) != 0;

		if (shift)
			res = (res << 1) & 0xFFFF;

		z = (res == 0);

		r[0] = res & 0xFF;
		r[1] = (res & 0xFF00) >>> 8;

		return 2;

	}

	/*
	 * Bit operations
	 */

	private int instructionBclr(int b) {

		switch (b) {

		case 0:
			c = false;
		case 1:
			z = false;
		case 2:
			n = false;
		case 3:
			v = false;
		case 4:
			s = false;
		case 5:
			h = false;
		case 6:
			t = false;
		case 7:
			i = false;

		}

		return 1;

	}

	private int instructionBset(int b) {

		switch (b) {

		case 0:
			c = true;
		case 1:
			z = true;
		case 2:
			n = true;
		case 3:
			v = true;
		case 4:
			s = true;
		case 5:
			h = true;
		case 6:
			t = true;
		case 7:
			i = true;

		}

		return 1;

	}

	private int instructionBld(int rd, int b) {

		if (t)
			r[rd] |= (1 << b);
		else
			r[rd] &= ~(1 << b);

		return 1;

	}

	private int instructionBst(int rd, int b) {

		t = (r[rd] & (1 << b)) != 0;

		return 1;

	}

	private int instructionLdX(int rd) {

		return instructionHelperLdSt(rd, X, false, false, false);

	}

	private int instructionLdDecX(int rd) {

		return instructionHelperLdSt(rd, X, false, true, false);

	}

	private int instructionLdXInc(int rd) {

		return instructionHelperLdSt(rd, X, false, false, true);

	}

	private int instructionLdDecY(int rd) {

		return instructionHelperLdSt(rd, Y, false, true, false);

	}

	private int instructionLdYInc(int rd) {

		return instructionHelperLdSt(rd, Y, false, false, true);

	}

	private int instructionLdDecZ(int rd) {

		return instructionHelperLdSt(rd, Z, false, true, false);

	}

	private int instructionLdZInc(int rd) {

		return instructionHelperLdSt(rd, Z, false, false, true);

	}

	private int instructionStX(int rd) {

		return instructionHelperLdSt(rd, X, true, false, false);

	}

	private int instructionStDecX(int rd) {

		return instructionHelperLdSt(rd, X, true, true, false);

	}

	private int instructionStXInc(int rd) {

		return instructionHelperLdSt(rd, X, true, false, true);

	}

	private int instructionStDecY(int rd) {

		return instructionHelperLdSt(rd, Y, true, true, false);

	}

	private int instructionStYInc(int rd) {

		return instructionHelperLdSt(rd, Y, true, false, true);

	}

	private int instructionStDecZ(int rd) {

		return instructionHelperLdSt(rd, Z, true, true, false);

	}

	private int instructionStZInc(int rd) {

		return instructionHelperLdSt(rd, Z, true, false, true);

	}

	private int instructionHelperLdSt(int rd, int ri, boolean store,
			boolean preDec, boolean postInc) {

		int address = r[ri] & (r[ri + 1] << 8);

		if (preDec)
			address--;

		if (store)
			dataWriteByte(address, (byte) r[rd]);
		else
			r[rd] = dataReadByte(address);

		if (postInc)
			address++;

		r[ri] = address & 0x0F;
		r[ri + 1] = (address & 0xF0) >>> 8;

		return 1; // FIXME: Cycle count not correct

	}

	private int instructionLddY(int rd, int k) {

		return instructionHelperLddStd(rd, k, Y, false);

	}

	private int instructionLddZ(int rd, int k) {

		return instructionHelperLddStd(rd, k, Z, false);

	}

	private int instructionStdY(int rd, int k) {

		return instructionHelperLddStd(rd, k, Y, true);

	}

	private int instructionStdZ(int rd, int k) {

		return instructionHelperLddStd(rd, k, Z, true);

	}

	private int instructionHelperLddStd(int rd, int k, int ri, boolean store) {

		int address = (r[ri] & (r[ri + 1] << 8)) + k;

		if (store)
			dataWriteByte(address, (byte) r[rd]);
		else
			r[rd] = dataReadByte(address);

		return 1; // FIXME: Cycle count not correct

	}

	private int instructionLpmR0() {

		return instructionHelperLpm(0, false);

	}

	private int instructionLpmZ(int rd) {

		return instructionHelperLpm(rd, false);

	}

	private int instructionLpmZInc(int rd) {

		return instructionHelperLpm(rd, true);

	}

	private int instructionHelperLpm(int rd, boolean postInc) {

		int address = r[Z] & (r[Z + 1] << 8);

		r[rd] = progMem[address];

		return 3;

	}

}
