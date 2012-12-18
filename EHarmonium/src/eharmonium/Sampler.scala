package eharmonium

import javax.sound.midi.MidiDevice
import javax.sound.midi.MidiSystem
import javax.sound.midi.Receiver
import javax.sound.midi.ShortMessage

object Sampler {
	private var recvDevice: MidiDevice = null
	private var recv: Receiver = null

	private val PAN_MSB = 10 // 0 .. 127
	private val PAN_LSB = 42 // 0 .. 127
	private val REVERB = 91 // 0 .. 127
	private val CHANNEL_VOLUME_MSB = 7
	private val CHANNEL_VOLUME_LSB = 39

	private val noOfChannels = 2
	
	private var overallVolume = 100 // 0..100
	private val volumes = Array.fill(noOfChannels)(100)
	
	private val notesPlaying = Array.fill(noOfChannels, 128)(false)
	
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
		
		// println("Sampler: channel " + channel + " volume " + actualVolume)
	}
	
	def noteOn(channel: Int, tone: Int) {
		val sm = new ShortMessage
		sm.setMessage(ShortMessage.NOTE_ON, channel, tone, 100)
		recv.send(sm, -1)
		notesPlaying(channel)(tone) = true
		// println("Sampler: channel " + channel + " tone " + tone + " on")
	}

	def noteOff(channel: Int, tone: Int) {
		val sm = new ShortMessage
		sm.setMessage(ShortMessage.NOTE_OFF, channel, tone, 0)
		recv.send(sm, -1)
		notesPlaying(channel)(tone) = false
		// println("Sampler: channel " + channel + " tone " + tone + " off")
	}
	
	def allNotesOff(channel: Int) {
		for (i <- 0 until notesPlaying(channel).length)
			if (notesPlaying(channel)(i))
				noteOff(channel, i)
	}
	
	def setOverallVolume(volume: Int) {
		overallVolume = volume
		
		if (overallVolume > 100) overallVolume = 100
		else if (overallVolume < 0) overallVolume = 0
		
		for (i <- 0 until volumes.length) {
			setVolume(i, volumes(i))			
		}

	}
	
	def getOverallVolume = overallVolume
}
