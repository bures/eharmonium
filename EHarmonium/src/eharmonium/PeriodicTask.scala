package eharmonium

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

object PeriodicTaskScheduler {
	val scheduler = Executors.newScheduledThreadPool(5);
	
	def shutdown() {
		scheduler.shutdown()
		if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
			scheduler.shutdownNow();

			if (!scheduler.awaitTermination(60, TimeUnit.SECONDS))
				Console.err.println("Pool did not terminate")
		}
	}
}

class PeriodicTask {
	private var task: Task = null
	
	private class Task(val taskFn: () => Unit, val period: Int) extends Runnable {
		var isCancelled = false
		val taskFuture: ScheduledFuture[Unit] = PeriodicTaskScheduler.scheduler.scheduleAtFixedRate(
				Task.this, 0, period, TimeUnit.MILLISECONDS).asInstanceOf[ScheduledFuture[Unit]]			
		
		override def run() {
			synchronized {
				if (!isCancelled)
					taskFn()
			}
		}
		
		def stop() {
			synchronized {
				isCancelled = true
				taskFuture.cancel(false)
			}
		}
		
		def doAsSynchronized(block : => Unit) {
			synchronized {
				block
			}
		}
		
	}
	
	def startTask(taskFn: () => Unit, period: Int) {
		assert(task == null)
		task = new Task(taskFn, period)
	}
	
	def stopTask() {
		if (task != null) {
			task.stop()
			task = null
		}		
	}
	
	def doAsSynchronized(block : => Unit) {
		if (task != null) {
			task.doAsSynchronized(block)
		} else {
			block
		}
	}
	
	def isStarted() = task != null
}

