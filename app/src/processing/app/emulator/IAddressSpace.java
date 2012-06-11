package processing.app.emulator;

public interface IAddressSpace {

	public byte readByte(int address);

	public void writeByte(int address, byte data);

}
