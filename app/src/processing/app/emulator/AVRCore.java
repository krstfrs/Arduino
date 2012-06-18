package processing.app.emulator;

public class AVRCore {

	private int ip;
	private int[] r = new int[32];

	private boolean h = false;
	private boolean s = false;
	private boolean v = false;
	private boolean n = false;
	private boolean z = false;
	private boolean c = false;

	private byte[] progmem;
	private IAddressSpace datamem;

	public AVRCore(byte[] progmem, IAddressSpace datamem) {

		this.progmem = progmem;
		this.datamem = datamem;

	}

	public void emulate(int cycles) {

		int c = 0;

		while (c < cycles)
			c += emulateInstruction();

	}

	private int emulateInstruction() {

		int instruction = (progmem[ip] << 8) & progmem[ip + 1];

		// NOP

		if (instruction == 0x0000)
			return 1;

		// 2-operand instructions
		// 0000 01rd dddd rrrr
		// 0000 1xrd dddd rrrr
		// 0001 xxrd dddd rrrr
		// 0010 xxrd dddd rrrr

		if ((instruction & 0xFC00) == 0x0400
				|| (instruction & 0xF800) == 0x0800
				|| (instruction & 0xF000) == 0x1000
				|| (instruction & 0xF000) == 0x2000) {

			int rr = ((instruction & 0x0200) >> 5) & (instruction & 0x000F);
			int rd = (instruction & 0x01F0) >> 4;

			int maskedInstruction = instruction & 0xFC00;

			switch (maskedInstruction) {

			case 0x1C00:
				return instructionAdc(rr, rd);
			case 0x0C00:
				return instructionAdd(rr, rd);
			case 0x2000:
				return instructionAnd(rr, rd);
			case 0x1400:
				return instructionCp(rr, rd);
			case 0x0400:
				return instructionCpc(rr, rd);
			case 0x1000:
				return instructionCpse(rr, rd);
			case 0x2400:
				return instructionEor(rr, rd);
			case 0x2C00:
				return instructionMov(rr, rd);
			case 0x2800:
				return instructionOr(rr, rd);
			case 0x0800:
				return instructionSbc(rr, rd);
			case 0x1800:
				return instructionSub(rr, rd);

			}

		}

		// 1-operand instructions
		// 1001 010d dddd 0xxx
		// 1001 010d dddd 1010 (DEC)

		if ((instruction & 0xFE08) == 0x9400
				|| (instruction & 0xFE0F) == 0x940A) {

			int rd = (instruction & 0x01F0) >> 4;

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

		if ((instruction & 0xF000) == 0x3000
				|| (instruction & 0xC000) == 0x8000) {

			int rd = ((instruction & 0x00F0) >> 4) + 0x10;
			int k = ((instruction & 0x0F00) >> 4) & (instruction & 0x000F);

			int maskedInstruction = instruction & 0xF000;

			switch (maskedInstruction) {

			case 0x7000:
				return instructionAndi(rd, k);
				// case 0x3000:
				// return instructionCpi(rd, k);
			case 0x6000:
				return instructionOri(rd, k);
				// case 0x4000:
				// return instructionSbci(rd, k);
				// case 0x9000:
				// return instructionSubi(rd, k);

			}

		}

		// ADIW/SBIW
		// 1001 011x kkpp kkkk

		if ((instruction & 0xFE00) == 0x9600) {

			int k = ((instruction & 0x00C0) >> 2) & (instruction & 0x000F);
			int rdp = (((instruction & 0x0030) >> 4) << 1) + 24;

			int maskedInstruction = instruction & 0xFE00;

			switch (maskedInstruction) {

			case 0x9600:
				instructionAdiw(rdp, k);
			case 0x9700:
				instructionSbiw(rdp, k);

			}

		}

		return 1;

	}

	/*
	 * The implemented instructions are grouped by type, however instructions
	 * not yet implemented are recorded here alphabetically.
	 */

	// TODO: Implement BCLR
	// TODO: Implement BLD
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
	// TODO: Implement BSET
	// TODO: Implement BST
	// TODO: Implement CALL
	// TODO: Implement CBI
	// TODO: Implement CBR
	// TODO: Implement CLC
	// TODO: Implement CLH
	// TODO: Implement CLI
	// TODO: Implement CLN
	// TODO: Implement CLR
	// TODO: Implement CLS
	// TODO: Implement CLT
	// TODO: Implement CLV
	// TODO: Implement CLZ
	// TODO: Implement CPI
	// TODO: Implement CPSE
	// TODO: Implement DES
	// TODO: Implement EICALL
	// TODO: Implement EIJMP
	// TODO: Implement ELMP
	// TODO: Implement FMUL
	// TODO: Implement FMULS
	// TODO: Implement FMULSU
	// TODO: Implement ICALL
	// TODO: Implement IJMP
	// TODO: Implement IN
	// TODO: Implement JMP
	// TODO: Implement LAC
	// TODO: Implement LAS
	// TODO: Implement LAT
	// TODO: Implement LD
	// TODO: Implement LDI
	// TODO: Implement LDS
	// TODO: Implement LPM
	// TODO: Implement MOVW
	// TODO: Implement MUL
	// TODO: Implement MULS
	// TODO: Implement MULSU
	// TODO: Implement NOP
	// TODO: Implement OUT
	// TODO: Implement POP
	// TODO: Implement PUSH
	// TODO: Implement RCALL
	// TODO: Implement RET
	// TODO: Implement RETI
	// TODO: Implement RJMP
	// TODO: Implement SBCI
	// TODO: Implement SBI
	// TODO: Implement SBIC
	// TODO: Implement SBIS
	// TODO: Implement SBR
	// TODO: Implement SBRC
	// TODO: Implement SBRS
	// TODO: Implement SEC
	// TODO: Implement SEH
	// TODO: Implement SEI
	// TODO: Implement SEN
	// TODO: Implement SER
	// TODO: Implement SES
	// TODO: Implement SET
	// TODO: Implement SEV
	// TODO: Implement SEZ
	// TODO: Implement SLEEP
	// TODO: Implement SPM
	// TODO: Implement ST
	// TODO: Implement STS
	// TODO: Implement SUBI
	// TODO: Implement WDR
	// TODO: Implement XCH

	private int instructionMov(int rr, int rd) {

		r[rd] = r[rr];

		return 1;

	}

	private int instructionAnd(int rr, int rd) {

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

	private int instructionOr(int rr, int rd) {

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

	private int instructionEor(int rr, int rd) {

		int res = r[rr] ^ r[rd];

		v = false;
		z = (res == 0);
		n = (res & 0x80) != 0;
		s = n ^ v;

		r[rd] = res;

		return 1;

	}

	private int instructionAdc(int rr, int rd) {

		return instructionAdcAdd(rr, rd, true);

	}

	private int instructionAdd(int rr, int rd) {

		return instructionAdcAdd(rr, rd, false);

	}

	private int instructionAdcAdd(int rr, int rd, boolean carry) {

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

	private int instructionCp(int rr, int rd) {

		return instructionCpCpcSbcSub(rr, rd, false, false);

	}

	private int instructionCpc(int rr, int rd) {

		return instructionCpCpcSbcSub(rr, rd, false, true);

	}

	private int instructionSbc(int rr, int rd) {

		return instructionCpCpcSbcSub(rr, rd, true, true);

	}

	private int instructionSub(int rr, int rd) {

		return instructionCpCpcSbcSub(rr, rd, true, false);

	}

	private int instructionCpCpcSbcSub(int rr, int rd, boolean save,
			boolean carry) {

		int cin = carry ? (c ? 1 : 0) : 0;

		int res = (r[rd] - r[rr] - cin) & 0xFF;

		boolean rr3 = (r[rr] & 0x08) != 0;
		boolean rd3 = (r[rd] & 0x08) != 0;
		boolean res3 = (res & 0x08) != 0;

		boolean rr7 = (r[rr] & 0x80) != 0;
		boolean rd7 = (r[rd] & 0x80) != 0;
		boolean res7 = (res & 0x80) != 0;

		h = (!rd3 && rr3) || (rr3 && res3) || (res3 && !rd3);
		c = (!rd7 && rr7) || (rr7 && res7) || (res7 && !rd7);
		v = (rd7 && !rr7 && !res7) || (!rd7 && rr7 && res7);
		z = (res == 0);
		n = res7;
		s = n ^ v;

		if (save)
			r[rd] = res;

		return 1;

	}

	private int instructionCpse(int rr, int rd) {

		throw new OpcodeNotImplementedException();

	}

	private int instructionAsr(int rd) {

		return instructionAsrLsrRor(rd, true, false);

	}

	private int instructionLsr(int rd) {

		return instructionAsrLsrRor(rd, false, false);

	}

	private int instructionRor(int rd) {

		return instructionAsrLsrRor(rd, false, true);

	}

	private int instructionAsrLsrRor(int rd, boolean arithmetic, boolean rotate) {

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

}
