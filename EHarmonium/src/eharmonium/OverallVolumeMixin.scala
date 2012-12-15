package eharmonium

trait OverallVolumeMixin {
  
  private val overallVolumeTimer = new PeriodicTask
  private var desiredOverallVolume = 100
  
  private def overallVolumeTaskFn() {
  	val overallVolume = Sampler.getOverallVolume
  	val dir = math.signum(desiredOverallVolume - overallVolume) 
  	
  	if (dir == 0) {
  		overallVolumeTimer.stopTask()
  	} else {
  		Sampler.setOverallVolume(overallVolume + dir)
  	}		
  }
  
  def changeOverallVolumeTo(volume: Int) {
  	desiredOverallVolume = volume
  	overallVolumeTimer.stopTask()
  	overallVolumeTimer.startTask(overallVolumeTaskFn, 10)		
  }
}