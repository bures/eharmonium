package eharmonium
import javax.sound.midi.MidiDevice
import javax.sound.midi.MidiSystem
import javax.sound.midi.Receiver
import javax.sound.midi.ShortMessage

object Sampler {
	var recvDevice: MidiDevice = null
	var recv: Receiver = null

	val PAN_MSB = 10 // 0 .. 127
	val PAN_LSB = 42 // 0 .. 127
	val REVERB = 91 // 0 .. 127
	val CHANNEL_VOLUME_MSB = 7
	val CHANNEL_VOLUME_LSB = 39

	var overallVolume = 100 // 0..100
	val volumes = Array(100, 100)
	
	def init() {
		val deviceInfo = MidiSystem.getMidiDeviceInfo.find(_.getName == "CoolSoft VirtualMIDISynth").orNull
		recvDevice = if (deviceInfo != null) MidiSystem.getMidiDevice(deviceInfo) else MidiSystem.getSynthesizer() 
		
		recvDevice.open()

		recv = recvDevice.getReceiver
	}

	def close() {
		recvDevice.close()
	}
	
	def setVolume(channel: Int, volume: Int) {
		volumes(channel) = volume

		val actualVolume = volume * overallVolume / 100
		
		val sm = new ShortMessage
		sm.setMessage(ShortMessage.CONTROL_CHANGE, channel, CHANNEL_VOLUME_MSB, actualVolume)
		recv.send(sm, -1)
		
		println("Sampler: vol " + actualVolume)
	}
	
	def noteOn(channel: Int, tone: Int) {
		val sm = new ShortMessage
		sm.setMessage(ShortMessage.NOTE_ON, channel, tone, 100)
		recv.send(sm, -1)
		println("Sampler: " + tone + " on")
	}

	def noteOff(channel: Int, tone: Int) {
		val sm = new ShortMessage
		sm.setMessage(ShortMessage.NOTE_OFF, channel, tone, 0)
		recv.send(sm, -1)
		println("Sampler: " + tone + " off")
	}
	
	def setOverallVolume(volume: Int) {
		overallVolume = volume
		
		if (overallVolume > 100) overallVolume = 100
		else if (overallVolume < 0) overallVolume = 0
		
		setVolume(0, volumes(0))
//		setVolume(1, volumes(1))
	}
	
	def getOverallVolume = overallVolume
}
