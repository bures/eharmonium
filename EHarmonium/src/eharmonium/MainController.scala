package eharmonium

import scala.collection.mutable.MutableList
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.stage.Stage
import javafx.scene.layout.VBox
import javafx.fxml.FXML
import javafx.scene.shape.Rectangle
import javafx.scene.paint.Paint
import javafx.scene.paint.Color
import javafx.application.Platform

class MainController(val stage: Stage) extends OverallVolumeMixin with ChordPlayerMixin {
	@FXML private val vBoxChords: VBox = null
	@FXML private val hBoxMajorChords: HBox = null
	@FXML private val hBoxMinorChords: HBox = null

	@FXML private val vBoxChords7: VBox = null
	@FXML private val hBoxMajor7Chords: HBox = null
	@FXML private val hBoxMinor7Chords: HBox = null
	
	@FXML private val vBoxChordsDim: VBox = null
	@FXML private val hBoxDim5Chords: HBox = null
	@FXML private val hBoxDimChords: HBox = null
	
	@FXML private val bellowsLevel: Rectangle = null
	
	private var isBacktickPressed = false // If backtick is pressed while pressing the chord already playing, 
	                              // the chord is first stopped, which causes is to be played with the
	                              // randomization of initial delays of it's tones
	
	var key: Int = 12 // Root tone
	var chordRootPos: Int = 6 // Position of the root major chord on the chord keyboard
	var chordRelativeHighestNote = 4
	
	var toneRootPos: Int = 7 // Position of the key note on the tone keyboard
	
	var chordKeyNowPlaying: ChordKeyController = null
	
	private var keyCodeModifier = 0
	private val chordKeys = MutableList.empty[ChordKeyController]
	private val toneKeys = MutableList.empty[ToneKeyController]
	
	private def updateKeys() {
		chordKeys.foreach(_.updateLabels())
		toneKeys.foreach(_.updateLabels())
	}
	
	private def resetAll() {
		if (chordKeyNowPlaying != null) 
			chordKeyNowPlaying.reset()
		else 
			chordPlayer.reset() // This is here to allows reseting bellows volume
		
		toneKeys.foreach(_.stop())
	}
	
	private def updateChordRowsOpacity() {
		keyCodeModifier match {
			case 0 => {
				vBoxChords.setOpacity(1)
				vBoxChords7.setOpacity(0)
				vBoxChordsDim.setOpacity(0)				
			}
			case KeyCodeModifier.SHIFT => {
				vBoxChords.setOpacity(0)
				vBoxChords7.setOpacity(1)
				vBoxChordsDim.setOpacity(0)				
			}
			case KeyCodeModifier.CTRL => {
				vBoxChords.setOpacity(0)
				vBoxChords7.setOpacity(0)
				vBoxChordsDim.setOpacity(1)				
			}
			case _ => {
				vBoxChords.setOpacity(0)
				vBoxChords7.setOpacity(0)
				vBoxChordsDim.setOpacity(0)				
			}
		}
	}

	@FXML
	private def handleKeyPressed(event: KeyEvent) {
		val keyCode = event.getCode
		
		keyCode match {
			case KeyCode.BACK_SPACE => 
				resetAll()
		
			case KeyCode.LEFT => {
				resetAll()
				key += 1
				updateKeys()
			}
			case KeyCode.RIGHT => {
				resetAll()
				key -= 1
				updateKeys()
			}
			case KeyCode.UP => {
				resetAll()
				chordRootPos = 6
				updateKeys()
			}
			case KeyCode.DOWN => {
				resetAll()
				chordRootPos = 9
				updateKeys()
			}
			case KeyCode.PAGE_DOWN => {
				resetAll()
				switchToNextChordPlayer()
			}
			case KeyCode.PAGE_UP => {
				resetAll()
				switchToPrevChordPlayer()
			}
			case KeyCode.SHIFT => {
				keyCodeModifier |= KeyCodeModifier.SHIFT
				updateChordRowsOpacity()
			}
			case KeyCode.CONTROL => {
				keyCodeModifier |= KeyCodeModifier.CTRL
				updateChordRowsOpacity()
			}
			case KeyCode.F9 => changeOverallVolumeTo(0)
			case KeyCode.F10 => changeOverallVolumeTo(25)
			case KeyCode.F11 => changeOverallVolumeTo(50)
			case KeyCode.F12 => changeOverallVolumeTo(100)

			case KeyCode.SPACE => 
				chordPlayer.spacePressed()

			case KeyCode.BACK_QUOTE =>
				isBacktickPressed = true
									
			case _ => {				
				chordKeys.
					filter(keyCtrl => keyCtrl.keyCode == keyCode && keyCtrl.keyCodeModifier == keyCodeModifier).
						foreach{ keyCtrl => 
							if (isBacktickPressed && keyCtrl == chordKeyNowPlaying && !chordKeyNowPlaying.isPressed) chordKeyNowPlaying.stop()
							keyCtrl.keyPressed()
						}
				
				toneKeys.filter(keyCtrl => keyCtrl.keyCode == keyCode).foreach(_.play())
			}
		}
	}
	
	@FXML 
	private def handleKeyReleased(event: KeyEvent) {
		val keyCode = event.getCode

		keyCode match {
			case KeyCode.SHIFT => {
				keyCodeModifier &= ~KeyCodeModifier.SHIFT
				updateChordRowsOpacity()
			}
			case KeyCode.CONTROL => {
				keyCodeModifier &= ~KeyCodeModifier.CTRL
				updateChordRowsOpacity()
			}

			case KeyCode.SPACE => 
				chordPlayer.spaceReleased()

			case KeyCode.BACK_QUOTE =>
				isBacktickPressed = false
									
			case _ => {
				chordKeys.
					filter(keyCtrl => keyCtrl.keyCode == keyCode).
						foreach(_.keyReleased())

				toneKeys.filter(keyCtrl => keyCtrl.keyCode == keyCode).foreach(_.play())
			}

		}
	}
	
	private def initialize() {
		buildChordKeys()
		vBoxChords7.setOpacity(0)
		vBoxChordsDim.setOpacity(0)

		buildToneKeys()
	}
	
	private def buildToneKeys() {
		def buildKeyRow(keyRow: Array[(KeyCode, String)], isUpperRow: Boolean, keyCodeModifier: Int, chordKind: ChordKind, hBox: HBox) {
			for (idx <- 0 until keyRow.length) {
				val (keyCode, keyName) = keyRow(idx)
				
				val fxmlLoader = new FXMLLoader(getClass().getResource("chordKey.fxml"))
				fxmlLoader.setController(new ChordKeyController(this, idx, keyCode, keyCodeModifier, keyName, chordKind))
				val pane = fxmlLoader.load().asInstanceOf[Pane]
				
				if (isUpperRow) 
					hBox.getChildren().add(idx, pane)
				else 
					hBox.getChildren().add(pane)
			
				chordKeys += fxmlLoader.getController[ChordKeyController]
			}
		}
		
		val upperRow = Array(
				KeyCode.DIGIT1 -> "1", KeyCode.DIGIT2 -> "2", KeyCode.DIGIT3 -> "3", KeyCode.DIGIT4 -> "4",
				KeyCode.DIGIT5 -> "5", KeyCode.DIGIT6 -> "6", KeyCode.DIGIT7 -> "7", KeyCode.DIGIT8 -> "8",
				KeyCode.DIGIT9 -> "9", KeyCode.DIGIT0 -> "0", KeyCode.MINUS -> "-", KeyCode.EQUALS -> "=")

		val lowerRow = Array(
				KeyCode.Q -> "Q", KeyCode.W -> "W", KeyCode.E -> "E", KeyCode.R -> "R",
				KeyCode.T -> "T", KeyCode.Y -> "Y", KeyCode.U -> "U", KeyCode.I -> "I",
				KeyCode.O -> "O", KeyCode.P -> "P", KeyCode.OPEN_BRACKET -> "[", KeyCode.CLOSE_BRACKET -> "]")

		buildKeyRow(upperRow, true, 0, CHORD_MAJOR, hBoxMajorChords)
		buildKeyRow(lowerRow, false, 0, CHORD_MINOR, hBoxMinorChords)
		buildKeyRow(upperRow, true, KeyCodeModifier.SHIFT, CHORD_MAJOR_7, hBoxMajor7Chords)
		buildKeyRow(lowerRow, false, KeyCodeModifier.SHIFT, CHORD_MINOR_7, hBoxMinor7Chords)
		buildKeyRow(upperRow, true, KeyCodeModifier.CTRL, CHORD_DIM5, hBoxDim5Chords)
		buildKeyRow(lowerRow, false, KeyCodeModifier.CTRL, CHORD_DIM, hBoxDimChords)
		
	}
	
	private def buildChordKeys() {
		def buildKeyRow(keyRow: Array[(KeyCode, String)], isUpperRow: Boolean, keyCodeModifier: Int, chordKind: ChordKind, hBox: HBox) {
			for (idx <- 0 until keyRow.length) {
				val (keyCode, keyName) = keyRow(idx)
				
				val fxmlLoader = new FXMLLoader(getClass().getResource("chordKey.fxml"))
				fxmlLoader.setController(new ChordKeyController(this, idx, keyCode, keyCodeModifier, keyName, chordKind))
				val pane = fxmlLoader.load().asInstanceOf[Pane]
				
				if (isUpperRow) 
					hBox.getChildren().add(idx, pane)
				else 
					hBox.getChildren().add(pane)
			
				chordKeys += fxmlLoader.getController[ChordKeyController]
			}
		}
		
		val upperRow = Array(
				KeyCode.DIGIT1 -> "1", KeyCode.DIGIT2 -> "2", KeyCode.DIGIT3 -> "3", KeyCode.DIGIT4 -> "4",
				KeyCode.DIGIT5 -> "5", KeyCode.DIGIT6 -> "6", KeyCode.DIGIT7 -> "7", KeyCode.DIGIT8 -> "8",
				KeyCode.DIGIT9 -> "9", KeyCode.DIGIT0 -> "0", KeyCode.MINUS -> "-", KeyCode.EQUALS -> "=")

		val lowerRow = Array(
				KeyCode.Q -> "Q", KeyCode.W -> "W", KeyCode.E -> "E", KeyCode.R -> "R",
				KeyCode.T -> "T", KeyCode.Y -> "Y", KeyCode.U -> "U", KeyCode.I -> "I",
				KeyCode.O -> "O", KeyCode.P -> "P", KeyCode.OPEN_BRACKET -> "[", KeyCode.CLOSE_BRACKET -> "]")

		buildKeyRow(upperRow, true, 0, CHORD_MAJOR, hBoxMajorChords)
		buildKeyRow(lowerRow, false, 0, CHORD_MINOR, hBoxMinorChords)
		buildKeyRow(upperRow, true, KeyCodeModifier.SHIFT, CHORD_MAJOR_7, hBoxMajor7Chords)
		buildKeyRow(lowerRow, false, KeyCodeModifier.SHIFT, CHORD_MINOR_7, hBoxMinor7Chords)
		buildKeyRow(upperRow, true, KeyCodeModifier.CTRL, CHORD_DIM5, hBoxDim5Chords)
		buildKeyRow(lowerRow, false, KeyCodeModifier.CTRL, CHORD_DIM, hBoxDimChords)
	}
	
	
	private object BellowsLevelUpdater extends Runnable {
		private val bellowsLevelColorBottom = Color.web("#2060ff")
		private val bellowsLevelColorTop = Color.web("#e02080")
		
		var level: Int = 0
		
		override def run() {
			val height = level * 105 / 1000
			
			bellowsLevel.setHeight(height)
			bellowsLevel.setY(105 - height)
			bellowsLevel.setFill(bellowsLevelColorBottom.interpolate(bellowsLevelColorTop, level / 1000.0))			
		}
	}
	
	override def setBellowsLevel(level: Double) {
		BellowsLevelUpdater.level = (level * 1000).floor.toInt
		Platform.runLater(BellowsLevelUpdater)
	}

}