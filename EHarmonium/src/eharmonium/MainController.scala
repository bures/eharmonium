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
import javafx.scene.image.ImageView
import org.jpedal.PdfDecoder
import org.jpedal.fonts.FontMappings
import java.io.File
import javafx.embed.swing.SwingFXUtils

class MainController(val stage: Stage) extends OverallVolumeMixin with PlayerMixin {
	@FXML val vBoxChords: VBox = null
	@FXML val hBoxMajorChords: HBox = null
	@FXML val hBoxMinorChords: HBox = null

	@FXML val vBoxChords7: VBox = null
	@FXML val hBoxMajor7Chords: HBox = null
	@FXML val hBoxMinor7Chords: HBox = null
	
	@FXML val vBoxChordsDim: VBox = null
	@FXML val hBoxDim5Chords: HBox = null
	@FXML val hBoxDimChords: HBox = null

	@FXML val hBoxBlackToneKeys: HBox = null
	@FXML val hBoxWhiteToneKeys: HBox = null
	
	@FXML val imgPDFPage: ImageView = null
	
	private var isBacktickPressed = false // If backtick is pressed while pressing the chord already playing, 
	                              // the chord is first stopped, which causes is to be played with the
	                              // randomization of initial delays of it's tones
	
	private var isSpacePressed = false;
	private var isTabPressed = false;
	
	var key: Int = 36 // Root tone
	var chordRootPos: Int = 6 // Position of the root major chord on the chord keyboard
	var chordRelativeHighestNote = 4
	
	var toneRootPos: Int = 6 // Position of the key note on the tone keyboard
	var toneOctaveStart: Int = 0 // A relative key w.r.t. C where the root note is positioned (0..13)   
	
	var chordKeyNowPlaying: ChordKeyController = null
	
	private var keyCodeModifier = 0
	private val chordKeys = MutableList.empty[ChordKeyController]
	private val toneKeys = MutableList.empty[ToneKeyController]
	
	private def updateKeys() {
		chordKeys.foreach(_.updateLabels())
		toneKeys.foreach(_.updateLabels())
	}
	
	private def stopAll() {
		if (chordKeyNowPlaying != null) 
			chordKeyNowPlaying.stop()
		
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
	def handleKeyPressed(event: KeyEvent) {
		val keyCode = event.getCode
		
		keyCode match {
			case KeyCode.BACK_SPACE => 
				if (chordKeyNowPlaying != null) 
					chordKeyNowPlaying.stop()
		
			case KeyCode.LEFT => {
				stopAll()
				key += 1
				updateKeys()
			}
			case KeyCode.RIGHT => {
				stopAll()
				key -= 1
				updateKeys()
			}
			case KeyCode.UP => {
				stopAll()
				chordRootPos = 6
				toneOctaveStart = 0
				updateKeys()
			}
			case KeyCode.DOWN => {
				stopAll()
				chordRootPos = 9
				toneOctaveStart = 10
				updateKeys()
			}
			case KeyCode.PAGE_DOWN => {
				stopAll()
				switchToNextPlayer()
			}
			case KeyCode.PAGE_UP => {
				stopAll()
				switchToPrevPlayer()
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

			case KeyCode.SPACE => {
				if (!isSpacePressed && !isTabPressed)
					player.spacePressed()

				isSpacePressed = true
			}

			case KeyCode.TAB => {
				if (!isSpacePressed && !isTabPressed)
					player.spacePressed()
				
				isTabPressed = true
			}

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
	def handleKeyReleased(event: KeyEvent) {
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

			case KeyCode.SPACE => {
				if (isSpacePressed && !isTabPressed)
					player.spaceReleased()
				
				isSpacePressed = false
			}

			case KeyCode.TAB => {
				if (!isSpacePressed && isTabPressed)
					player.spaceReleased()
				
				isTabPressed = false
			}

			case KeyCode.BACK_QUOTE =>
				isBacktickPressed = false
									
			case _ => {
				chordKeys.
					filter(keyCtrl => keyCtrl.keyCode == keyCode).
						foreach(_.keyReleased())

				toneKeys.filter(keyCtrl => keyCtrl.keyCode == keyCode).foreach(_.stop())
			}

		}
	}
	
	def initialize() {
		buildChordKeys()
		vBoxChords7.setOpacity(0)
		vBoxChordsDim.setOpacity(0)

		buildToneKeys()

		
		val decoder = new PdfDecoder(true)
		FontMappings.setFontReplacements()
		decoder.openPdfFile("songbook" + File.separator + "HarmoniumEng.pdf")
		decoder.setPageParameters(1.5f, 181)
		val imgAWT = decoder.getPageAsImage(181)
		decoder.closePdfFile()
		val imgFX = SwingFXUtils.toFXImage(imgAWT, null)
		imgPDFPage.setImage(imgFX)
		imgPDFPage.setFitWidth(imgFX.getWidth)
		imgPDFPage.setFitHeight(imgFX.getHeight)

	}
	
	private def buildToneKeys() {
		def buildKeyRow(keyRow: Array[(KeyCode, String)], isUpperRow: Boolean, hBox: HBox) {
			for (idx <- 0 until keyRow.length) {
				val (keyCode, keyName) = keyRow(idx)
				
				val fxmlLoader = new FXMLLoader(getClass().getResource("toneKey.fxml"))
				
				val keyIdx = if (isUpperRow) idx * 2 + 1 else idx * 2
				
				fxmlLoader.setController(new ToneKeyController(this, keyIdx, keyCode, keyName))
				val pane = fxmlLoader.load().asInstanceOf[Pane]
				
				if (!isUpperRow) 
					hBox.getChildren().add(idx, pane)
				else 
					hBox.getChildren().add(pane)
			
				toneKeys += fxmlLoader.getController[ToneKeyController]
			}
		}
		
		val blackRow = Array(
				KeyCode.A -> "A", KeyCode.S -> "S", KeyCode.D -> "D", KeyCode.F -> "F",
				KeyCode.G -> "G", KeyCode.H -> "H", KeyCode.J -> "J", KeyCode.K -> "K",
				KeyCode.L -> "L", KeyCode.SEMICOLON -> ";", KeyCode.QUOTE -> "'")

		val whiteRow = Array(
				KeyCode.LESS -> "\\", KeyCode.Z -> "Z", KeyCode.X -> "X", KeyCode.C -> "C",
				KeyCode.V -> "V", KeyCode.B -> "B", KeyCode.N -> "N", KeyCode.M -> "M",
				KeyCode.COMMA -> ",", KeyCode.PERIOD -> ".", KeyCode.SLASH -> "/")

		buildKeyRow(blackRow, true, hBoxBlackToneKeys)
		buildKeyRow(whiteRow, false, hBoxWhiteToneKeys)
		
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

}