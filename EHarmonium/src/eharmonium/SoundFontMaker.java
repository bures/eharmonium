package eharmonium;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.*;

import com.sun.media.sound.*;

public class SoundFontMaker {

	/* Requires the following in Eclipse: 
	 * Window > Preferences > Java > Compiler > Errors/Warnings > Deprecated and restricted API > Forbidden reference (access rules) > set it to ‘Warnings’ 
	 */
	
	SF2Instrument ins;
	SF2Soundbank sf2;
	SF2Layer layer;
	
	final static String[] keys = new String[] {
		"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
	};
	

	public int getPitch(String fileName) {
		for (int key = 0; key < keys.length; key++) {
			try {
				if (fileName.startsWith(keys[key])) {
					int base = Integer.parseInt(fileName.substring(keys[key].length(), keys[key].length() + 1));
					
					return base * 12 + 12 + key;
				}
			} catch (NumberFormatException e) {
			}
		}
		
		return -1;
	}
	
	public void addSample(File audioFile, int pitch) throws UnsupportedAudioFileException, IOException {
		AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);

		AudioFormat format = new AudioFormat(audioStream.getFormat().getSampleRate(), 16, 1, true, false);
		AudioInputStream convAudioStream = AudioSystem.getAudioInputStream(format, audioStream);

		int datalength = (int) convAudioStream.getFrameLength() * format.getFrameSize();
		byte[] data = new byte[datalength];
		convAudioStream.read(data, 0, data.length);
		audioStream.close();

		SF2Sample sample = new SF2Sample(sf2);
		sample.setName(audioFile.getName());
		sample.setData(data);
		sample.setSampleRate((long) format.getSampleRate());
		sample.setOriginalPitch(pitch);
		sample.setStartLoop(0);
		sample.setEndLoop(convAudioStream.getFrameLength());
		sf2.addResource(sample);

		SF2LayerRegion region = new SF2LayerRegion();
		region.putBytes(SF2Region.GENERATOR_KEYRANGE, new byte[] {(byte)pitch, (byte)pitch});		

		region.putShort(SF2Region.GENERATOR_ATTACKVOLENV, (short)(1200 * Math.log(0.15) / Math.log(2)));
		region.putShort(SF2Region.GENERATOR_RELEASEVOLENV, (short)(1200 * Math.log(0.01) / Math.log(2)));
		/*
		region.putShort(SF2Region.GENERATOR_HOLDVOLENV, (short)(1200 * Math.log(0.2) / Math.log(2)));
		region.putShort(SF2Region.GENERATOR_DECAYVOLENV, (short)(1200 * Math.log(15) / Math.log(2)));
		region.putShort(SF2Region.GENERATOR_SUSTAINVOLENV, (short)(2 * 10)); 
		region.putShort(SF2Region.GENERATOR_RELEASEVOLENV, (short)(1200 * Math.log(0.01) / Math.log(2)));

		region.putShort(SF2Region.GENERATOR_ATTACKMODENV, (short)(1200 * Math.log(0.15) / Math.log(2)));
		region.putShort(SF2Region.GENERATOR_HOLDMODENV, (short)(1200 * Math.log(0.2) / Math.log(2)));
		region.putShort(SF2Region.GENERATOR_DECAYMODENV, (short)(1200 * Math.log(1.5) / Math.log(2)));
		region.putShort(SF2Region.GENERATOR_SUSTAINMODENV, (short)(100 * 10)); // 100%
		region.putShort(SF2Region.GENERATOR_MODENVTOPITCH, (short)(2));
		region.putShort(SF2Region.GENERATOR_RELEASEMODENV, (short)(1200 * Math.log(0.01) / Math.log(2)));
*/
		region.putShort(SF2Region.GENERATOR_DELAYMODLFO, (short)(1200 * Math.log(0) / Math.log(2)));
		region.putShort(SF2Region.GENERATOR_FREQMODLFO, (short)(1200 * Math.log(0.8 / 8.176) / Math.log(2)));
		region.putShort(SF2Region.GENERATOR_DECAYMODENV, (short)(1200 * Math.log(1.5) / Math.log(2)));
		region.putShort(SF2Region.GENERATOR_MODLFOTOVOLUME, (short)(1 * 10)); 
		region.putShort(SF2Region.GENERATOR_MODLFOTOPITCH, (short)(1));

		region.putInteger(SF2Region.GENERATOR_SAMPLEMODES, 1);
		region.setSample(sample);
	
		layer.getRegions().add(region);
	}
	
	public void createSoundFont(File loopsDir, File outSoundFontFile) throws IOException, UnsupportedAudioFileException {
		SF2InstrumentRegion insregion;
		
		sf2 = new SF2Soundbank();

		layer = new SF2Layer(sf2);
		layer.setName("Harmonium"); // Instrument
		sf2.addResource(layer);

		for (String fileName: loopsDir.list()) {
			if (fileName.endsWith(".wav")) {
				int pitch = getPitch(fileName);
				if (pitch == -1) {
					System.out.println("Ignoring file " + fileName);
				} else {
					System.out.println("Processing file " + fileName);
					addSample(new File(loopsDir, fileName), pitch);
				}
			}
		}

		ins = new SF2Instrument(sf2);
		ins.setName("Harmonium 1"); // Preset
		insregion = new SF2InstrumentRegion();
		insregion.setLayer(layer);
		ins.getRegions().add(insregion);
		sf2.addInstrument(ins);

		ins = new SF2Instrument(sf2);
		ins.setName("Harmonium 2"); // Preset
		insregion = new SF2InstrumentRegion();
		insregion.setLayer(layer);
		ins.getRegions().add(insregion);
		sf2.addInstrument(ins);
		
		sf2.save(outSoundFontFile);
	}
	
	public static void main(String[] args) throws UnsupportedAudioFileException, IOException {
		new SoundFontMaker().createSoundFont(new File("loops"), new File("harmonium.sf2"));
	}

}
