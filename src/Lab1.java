import TSim.*;

public class Lab1 {

  public Lab1(Integer speed1, Integer speed2) {
    TSimInterface tsi = TSimInterface.getInstance();

    try {
      tsi.setSpeed(1,speed1);
    }
    catch (CommandException e) {
      e.printStackTrace();    // or only e.getMessage() for the error
      System.exit(1);
    }
  }

  class Train extends Thread{

    Integer id;
    Integer speed;
    TSimInterface tsi;
    Train(Integer id, Integer startSpeed, TSimInterface tsi){
      this.id=id;
      this.speed = startSpeed;
      this.tsi = tsi;
    }
    public void run(){
      while (true){

      }

    }
    public void waitAtStation(){
      try {
        this.sleep(1000 + (20 *speed));
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
