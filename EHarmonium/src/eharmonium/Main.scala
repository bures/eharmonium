package eharmonium
import javafx.stage.Stage
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.Parent
import javafx.application.Application
import javafx.stage.StageStyle
import javafx.event.EventHandler
import javafx.stage.WindowEvent

class Main extends Application {
	override def start(stage: Stage) {
		Sampler.init()
		
		// stage.initStyle(StageStyle.TRANSPARENT);
		
		val fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"))
		fxmlLoader.setController(new MainController(stage));
		
		val root = fxmlLoader.load().asInstanceOf[Parent] 

		stage.setOnCloseRequest(new EventHandler[WindowEvent] {			
			override def handle(event: WindowEvent) {
				PeriodicTaskScheduler.shutdown();
			}
		});
		
		stage.setTitle("E-Harmonium");
		stage.setScene(new Scene(root, 1052, 225))
		stage.show();
	}
	
	override def stop() {
		Sampler.close()
	}	
}

object Main extends scala.App {
	Application.launch(classOf[Main], args : _*)
}