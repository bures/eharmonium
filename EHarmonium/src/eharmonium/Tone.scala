package eharmonium

object Tone {
	private val firstToneNo = 24
	private val lastToneNo = 107
	
	private val abstractNames = Array("I", "ii", "II", "iii", "III", "IV", "v", "V", "vi", "VI", "vii", "VII")
	private val actualNames = Array("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "H")
	
	private def buildTones = {
		val tones = (firstToneNo to lastToneNo) map {new Tone(_)}
		tones.toArray
	}
	
	val tones = buildTones
	
	def apply(toneNo: Int) = if (toneNo < firstToneNo || toneNo > lastToneNo) EmptyTone else tones(toneNo - firstToneNo)
}

object EmptyTone extends Tone(-1) {
	override def isEmpty = true
	override def name = sys.error("Unsupported function.")
	override def abstractName(baseKey: Int) = sys.error("Unsupported function.")
	override def octave = sys.error("Unsupported function.")
}

class Tone(val toneNo: Int) {
	override def toString = name
	def name = Tone.actualNames(toneNo % 12) + "-" + octave
	def abstractName(baseKey: Int) = Tone.abstractNames((120000 + toneNo - baseKey) % 12)

	def octave = (toneNo - 12) / 12
	
	def isEmpty = false
}
