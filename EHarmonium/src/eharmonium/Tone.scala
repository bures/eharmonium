package eharmonium

object Tone {
	private val abstractNames = Array("I", "ii", "II", "iii", "III", "IV", "v", "V", "vi", "VI", "vii", "VII")
	private val actualNames = Array("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "H")
	
	private def buildTones(baseFirst: Int, baseLast: Int) = {
		val tones = (baseFirst to baseLast) map {new Tone(_)}
		tones.toArray
	}
	
	val tones = buildTones(24, 107)
	
	def apply(toneNo: Int) = tones(toneNo)
}

class Tone(val toneNo: Int) {
	override def toString = name
	def name = Tone.actualNames(toneNo % 12) + "-" + octave
	def abstractName(baseKey: Int) = Tone.abstractNames((120000 + toneNo - baseKey) % 12)

	def octave = (toneNo - 12) / 12
}
