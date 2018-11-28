package se.qxx.jukebox.converter;

public class ConversionProbeResult {

	private String targetVideoCodec;
	private String targetAudioCodec;
	private boolean needsConversion;
	
	public String getTargetVideoCodec() {
		return targetVideoCodec;
	}

	public void setTargetVideoCodec(String targetVideoCodec) {
		this.targetVideoCodec = targetVideoCodec;
	}

	public String getTargetAudioCodec() {
		return targetAudioCodec;
	}

	public void setTargetAudioCodec(String targetAudioCodec) {
		this.targetAudioCodec = targetAudioCodec;
	}

	public ConversionProbeResult(boolean needsConversion, String targetVideoCodec, String targetAudioCodec) {
		this.setNeedsConversion(needsConversion);
		this.setTargetAudioCodec(targetAudioCodec);
		this.setTargetVideoCodec(targetVideoCodec);
	}

	public boolean getNeedsConversion() {
		return needsConversion;
	}

	public void setNeedsConversion(boolean needsConversion) {
		this.needsConversion = needsConversion;
	}
}
