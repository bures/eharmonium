package eharmonium

import java.util.Timer
import java.util.TimerTask
import CurrentPlayStyle.playStyle

abstract class ChordKind(val kindName: String, val nameSuffix: String, val pattern: List[Int], val relPositionToMajor: Int)
object CHORD_MAJOR extends ChordKind("Major", "", List(0, 4, 7), 0)
object CHORD_MINOR extends ChordKind("Minor", "m", List(0, 3, 7), 3)
object CHORD_MAJOR_7 extends ChordKind("Major7", "7", List(0, 4, 7, 10), 0)
object CHORD_MINOR_7 extends ChordKind("Minor7", "m7", List(0, 3, 7, 10), 3)
object CHORD_DIM5 extends ChordKind("Dim 5th", "5b", List(0, 4, 6), 0)
object CHORD_DIM extends ChordKind("Dim", "�", List(0, 3, 6), 0)

object Chord {
	val abstractNames = Array("I", "ii", "II", "iii", "III", "IV", "v", "V", "vi", "VI", "vii", "VII")
	val chordNames = Array("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "H")
	
	def buildChords(baseFirst: Int, baseLast: Int, kind: ChordKind) = {
		val chords = (baseFirst to baseLast) map {new Chord(_, kind)}
		chords.toArray
	}
	
	val chords = Map(
		CHORD_MAJOR -> buildChords(24, 107, CHORD_MAJOR),
		CHORD_MINOR -> buildChords(24, 107, CHORD_MINOR),	
		CHORD_MAJOR_7 -> buildChords(24, 107, CHORD_MAJOR_7),
		CHORD_MINOR_7 -> buildChords(24, 107, CHORD_MINOR_7),		
		CHORD_DIM5 -> buildChords(24, 107, CHORD_DIM5),
		CHORD_DIM -> buildChords(24, 107, CHORD_DIM)	
	)

	def apply(base: Int, kind: ChordKind) = chords(kind)(base)

	def nthFifthFrom(base: Int, kind: ChordKind, n: Int, relativeHighestNote: Int) = Chord( 
		base + ((if (n < 0) (-n) * 5 else n * 7) + 11 - relativeHighestNote) % 12 - 11 + relativeHighestNote,
		kind)

}

class Chord(val base: Int, val kind: ChordKind) {
	override def toString = name
	def name = Chord.chordNames(base % 12) + kind.nameSuffix + "-" + octave
	def abstractName(baseKey: Int) = Chord.abstractNames((120000 + base - baseKey) % 12) + kind.nameSuffix

	def octave = (base - 12) / 12
	
	def tones = kind.pattern map {_ + base}
	
	def play() = playStyle.play(base, tones)
	def pressed() = playStyle.pressed()
	def released() = playStyle.released()
	def stop() = playStyle.stop()
	def reset() = playStyle.reset()
}