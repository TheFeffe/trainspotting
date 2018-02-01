import TSim.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import static java.util.Arrays.asList;
import static java.util.Arrays.fill;

import java.util.stream.Collectors;


public class Lab1 {


    enum SensorName {
        CROSSING_NORTH, CROSSING_WEST, CROSSING_SOUTH, CROSSING_EAST,
        NORTH_JUNCTION_FROM_NORTH_WEST, NORTH_JUNCTION_FROM_SOUTH_WEST, NORTH_JUNCTION_FROM_EAST,
        EAST_PITSTOP_FROM_EAST, EAST_PITSTOP_FROM_NORTH_WEST, EAST_PITSTOP_FROM_SOUTH_WEST,
        WEST_PITSTOP_FROM_NORTH_EAST, WEST_PITSTOP_FROM_SOUTH_EAST, WEST_PITSTOP_FROM_WEST,
        SOUTH_JUNCTION_FROM_WEST, SOUTH_JUNCTION_FROM_NORTH_EAST, SOUTH_JUNCTION_FROM_SOUTH_EAST,
        NORTH_STATION_NORTH, NORTH_STATION_SOUTH, SOUTH_STATION_NORTH, SOUTH_STATION_SOUTH

    }

    enum SwitchName {
        NORTH, SOUTH, PITSTOP_EAST, PITSTOP_WEST
    }

    enum SemaphoreName {
        CROSSING, NORTH, EAST, WEST, PITSTOP, SOUTH
    }

    public Map<SensorName, List<Integer>> sensors = new HashMap<SensorName, List<Integer>>();
    public Map<SwitchName, List<Integer>> switches = new HashMap<SwitchName, List<Integer>>();
    public Map<SemaphoreName, Semaphore> semaphores = new HashMap<SemaphoreName, Semaphore>();
    public Map<List<Integer>, SensorName> sensorsInversed = new HashMap<List<Integer>, SensorName>();


    public Lab1(Integer speed1, Integer speed2) {


        TSimInterface tsi = TSimInterface.getInstance();


        semaphores.put(SemaphoreName.CROSSING, new Semaphore(1));
        semaphores.put(SemaphoreName.NORTH, new Semaphore(0));
        semaphores.put(SemaphoreName.SOUTH, new Semaphore(0));
        semaphores.put(SemaphoreName.EAST, new Semaphore(1));
        semaphores.put(SemaphoreName.WEST, new Semaphore(1));
        semaphores.put(SemaphoreName.PITSTOP, new Semaphore(1));


        switches.put(SwitchName.NORTH, asList(17, 7));
        switches.put(SwitchName.PITSTOP_EAST, asList(15, 9));
        switches.put(SwitchName.PITSTOP_WEST, asList(4, 9));
        switches.put(SwitchName.SOUTH, asList(3, 11));


        sensors.put(SensorName.CROSSING_NORTH, asList(11, 5));
        sensors.put(SensorName.CROSSING_WEST, asList(6, 3));
        sensors.put(SensorName.CROSSING_SOUTH, asList(11, 8));
        sensors.put(SensorName.CROSSING_EAST, asList(11, 7));

        sensors.put(SensorName.NORTH_JUNCTION_FROM_NORTH_WEST, asList(14, 7));
        sensors.put(SensorName.NORTH_JUNCTION_FROM_SOUTH_WEST, asList(14, 8));
        sensors.put(SensorName.NORTH_JUNCTION_FROM_EAST, asList(19, 8));

        sensors.put(SensorName.EAST_PITSTOP_FROM_EAST, asList(18, 9));
        sensors.put(SensorName.EAST_PITSTOP_FROM_NORTH_WEST, asList(11, 9));
        sensors.put(SensorName.EAST_PITSTOP_FROM_SOUTH_WEST, asList(11, 10));

        sensors.put(SensorName.WEST_PITSTOP_FROM_NORTH_EAST, asList(8, 9));
        sensors.put(SensorName.WEST_PITSTOP_FROM_SOUTH_EAST, asList(8, 10));
        sensors.put(SensorName.WEST_PITSTOP_FROM_WEST, asList(1, 9));

        sensors.put(SensorName.SOUTH_JUNCTION_FROM_NORTH_EAST, asList(8, 11));
        sensors.put(SensorName.SOUTH_JUNCTION_FROM_SOUTH_EAST, asList(8, 13));
        sensors.put(SensorName.SOUTH_JUNCTION_FROM_WEST, asList(1, 11));

        sensors.put(SensorName.NORTH_STATION_NORTH, asList(14, 3));
        sensors.put(SensorName.NORTH_STATION_SOUTH, asList(14, 5));
        sensors.put(SensorName.SOUTH_STATION_NORTH, asList(14, 11));
        sensors.put(SensorName.SOUTH_STATION_SOUTH, asList(14, 13));


        for (Map.Entry<SensorName, List<Integer>> entry : sensors.entrySet()) {
            sensorsInversed.put(entry.getValue(), entry.getKey());
        }


        Train train1 = null;
        Train train2 = null;
        try {
            train1 = new Train(1, speed1, tsi, SensorName.NORTH_STATION_NORTH);
            train2 = new Train(2, speed2, tsi, SensorName.SOUTH_STATION_NORTH);
        } catch (CommandException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        train1.start();
        train2.start();


    }


    class Train extends Thread {

        Integer id;
        Integer speed;
        //Comparative direction, 1 is starting direction, -1 is the opposite.
        //Because of how the track is designed the trains can only change direction when at a complete stop.
        Integer direction = 1;
        TSimInterface tsi;
        SensorName lastSensor;

        int maxSpeed = 28;

        Train(Integer id, Integer startSpeed, TSimInterface tsi, SensorName startSensor) throws CommandException, InterruptedException {
            this.id = id;
            if (startSpeed <= maxSpeed)
                this.speed = startSpeed;
            else
                this.speed = maxSpeed;
            this.tsi = tsi;
            this.lastSensor = startSensor;


            try {
                tsi.setSpeed(id, direction * speed);
            } catch (CommandException e) {
                e.printStackTrace();
            }
        }

        private void setSwitch(SwitchName switchName, int direction) throws CommandException {

            List<Integer> switchPos = switches.get(switchName);
            tsi.setSwitch(switchPos.get(0), switchPos.get(1), direction);
        }

        private void waitForPass(SemaphoreName semaphoreName) throws CommandException, InterruptedException {
            Semaphore semaphore = semaphores.get(semaphoreName);
            tsi.setSpeed(id, 0);
            semaphore.acquire();
            tsi.setSpeed(id, this.direction * speed);
        }

        private void waitForPass(SemaphoreName semaphoreName, SwitchName switchName, int direction) throws CommandException, InterruptedException {
            Semaphore semaphore = semaphores.get(semaphoreName);
            tsi.setSpeed(id, 0);
            semaphore.acquire();
            setSwitch(switchName, direction);
            tsi.setSpeed(id, this.direction * speed);

        }

        private void releasePass(SemaphoreName semaphoreName) {
            Semaphore semaphore = semaphores.get(semaphoreName);
            if (semaphore.availablePermits() == 0) {
                semaphore.release();
            }
        }

        private boolean semaphoreHasAvailablePermits(SemaphoreName semaphoreName) {
            return 0 != semaphores.get(semaphoreName).availablePermits();
        }


        private void handleSensorEvent(SensorEvent sensorEvent) throws CommandException, InterruptedException {
            boolean active = sensorEvent.getStatus() == SensorEvent.ACTIVE;
            List<Integer> sensorPos = asList(sensorEvent.getXpos(), sensorEvent.getYpos());

            SensorName sensorName = sensorsInversed.get(sensorPos);
            if (active) {
                switch (sensorName) {
                    //CROSSING
                    case CROSSING_EAST:
                        if (lastSensor != SensorName.CROSSING_WEST) {
                            waitForPass(SemaphoreName.CROSSING);
                        } else {
                            releasePass(SemaphoreName.CROSSING);
                        }
                        break;
                    case CROSSING_WEST:
                        if (lastSensor == SensorName.CROSSING_EAST) {
                            releasePass(SemaphoreName.CROSSING);
                        } else {
                            waitForPass(SemaphoreName.CROSSING);
                        }
                        break;
                    case CROSSING_NORTH:
                        if (lastSensor != SensorName.CROSSING_SOUTH) {
                            waitForPass(SemaphoreName.CROSSING);
                        } else {
                            releasePass(SemaphoreName.CROSSING);
                        }
                        break;
                    case CROSSING_SOUTH:
                        if (lastSensor != SensorName.CROSSING_NORTH) {
                            waitForPass(SemaphoreName.CROSSING);
                        } else {
                            releasePass(SemaphoreName.CROSSING);
                        }
                        break;

                    //NORTH JUNCTION
                    case NORTH_JUNCTION_FROM_EAST:
                        if (lastSensor == SensorName.EAST_PITSTOP_FROM_EAST) {
                            if (semaphoreHasAvailablePermits(SemaphoreName.NORTH)) {
                                waitForPass(SemaphoreName.NORTH, SwitchName.NORTH, TSimInterface.SWITCH_RIGHT);
                            } else {
                                setSwitch(SwitchName.NORTH, TSimInterface.SWITCH_LEFT);
                            }
                        } else if (lastSensor == SensorName.NORTH_JUNCTION_FROM_NORTH_WEST) {
                            releasePass(SemaphoreName.NORTH);
                        }
                        break;

                    case NORTH_JUNCTION_FROM_NORTH_WEST:
                        if (lastSensor != SensorName.NORTH_JUNCTION_FROM_EAST) {
                            waitForPass(SemaphoreName.EAST, SwitchName.NORTH, TSimInterface.SWITCH_RIGHT);
                        } else
                            releasePass(SemaphoreName.EAST);
                        break;
                    case NORTH_JUNCTION_FROM_SOUTH_WEST:
                        if (lastSensor == SensorName.NORTH_JUNCTION_FROM_EAST) {
                            releasePass(SemaphoreName.EAST);
                        } else {
                            waitForPass(SemaphoreName.EAST, SwitchName.NORTH, TSimInterface.SWITCH_LEFT);
                        }
                        break;

                    //EAST PITSTOP
                    case EAST_PITSTOP_FROM_EAST:
                        if (lastSensor == SensorName.NORTH_JUNCTION_FROM_EAST) {
                            if (semaphoreHasAvailablePermits(SemaphoreName.PITSTOP)) {
                                waitForPass(SemaphoreName.PITSTOP, SwitchName.PITSTOP_EAST, TSimInterface.SWITCH_RIGHT);
                            } else {
                                setSwitch(SwitchName.PITSTOP_EAST, TSimInterface.SWITCH_LEFT);
                            }
                        } else if (lastSensor == SensorName.EAST_PITSTOP_FROM_NORTH_WEST) {
                            releasePass(SemaphoreName.PITSTOP);
                        }
                        break;
                    case EAST_PITSTOP_FROM_NORTH_WEST:
                        if (lastSensor != SensorName.EAST_PITSTOP_FROM_EAST) {
                            waitForPass(SemaphoreName.EAST, SwitchName.PITSTOP_EAST, TSimInterface.SWITCH_RIGHT);
                        } else {
                            releasePass(SemaphoreName.EAST);
                        }
                        break;
                    case EAST_PITSTOP_FROM_SOUTH_WEST:
                        if (lastSensor == SensorName.EAST_PITSTOP_FROM_EAST) {
                            releasePass(SemaphoreName.EAST);
                        } else {
                            waitForPass(SemaphoreName.EAST, SwitchName.PITSTOP_EAST, TSimInterface.SWITCH_LEFT);
                        }
                        break;

                    //WEST PITSTOP
                    case WEST_PITSTOP_FROM_NORTH_EAST:
                        if (lastSensor != SensorName.WEST_PITSTOP_FROM_WEST) {
                            waitForPass(SemaphoreName.WEST, SwitchName.PITSTOP_WEST, TSimInterface.SWITCH_LEFT);
                        } else {
                            releasePass(SemaphoreName.WEST);
                        }
                        break;
                    case WEST_PITSTOP_FROM_SOUTH_EAST:
                        if (lastSensor != SensorName.WEST_PITSTOP_FROM_WEST) {
                            waitForPass(SemaphoreName.WEST, SwitchName.PITSTOP_WEST, TSimInterface.SWITCH_RIGHT);
                        } else {
                            releasePass(SemaphoreName.WEST);
                        }
                        break;
                    case WEST_PITSTOP_FROM_WEST:
                        if (lastSensor == SensorName.SOUTH_JUNCTION_FROM_WEST) {
                            if (semaphoreHasAvailablePermits(SemaphoreName.PITSTOP)) {
                                waitForPass(SemaphoreName.PITSTOP, SwitchName.PITSTOP_WEST, TSimInterface.SWITCH_LEFT);
                            } else {
                                setSwitch(SwitchName.PITSTOP_WEST, TSimInterface.SWITCH_RIGHT);
                            }
                        } else if (lastSensor == SensorName.WEST_PITSTOP_FROM_NORTH_EAST) {
                            releasePass(SemaphoreName.PITSTOP);
                        }
                        break;

                    //SOUTH JUNCTION
                    case SOUTH_JUNCTION_FROM_WEST:
                        if (lastSensor == SensorName.WEST_PITSTOP_FROM_WEST) {
                            if (semaphoreHasAvailablePermits(SemaphoreName.SOUTH)) {
                                waitForPass(SemaphoreName.SOUTH, SwitchName.SOUTH, TSimInterface.SWITCH_LEFT);
                            } else {
                                setSwitch(SwitchName.SOUTH, TSimInterface.SWITCH_RIGHT);
                            }
                        } else if (lastSensor == SensorName.SOUTH_JUNCTION_FROM_NORTH_EAST) {
                            releasePass(SemaphoreName.SOUTH);
                        }
                        break;
                    case SOUTH_JUNCTION_FROM_NORTH_EAST:
                        if (lastSensor == SensorName.SOUTH_JUNCTION_FROM_WEST) {
                            releasePass(SemaphoreName.WEST);
                        } else {
                            waitForPass(SemaphoreName.WEST, SwitchName.SOUTH, TSimInterface.SWITCH_LEFT);
                        }
                        break;
                    case SOUTH_JUNCTION_FROM_SOUTH_EAST:
                        if (lastSensor == SensorName.SOUTH_JUNCTION_FROM_WEST) {
                            releasePass(SemaphoreName.WEST);
                        } else {
                            waitForPass(SemaphoreName.WEST, SwitchName.SOUTH, TSimInterface.SWITCH_RIGHT);
                        }
                        break;


                    //NORTH STATION
                    case NORTH_STATION_NORTH:
                        if (lastSensor == SensorName.CROSSING_WEST) {
                            waitAtStation();
                        }
                        break;
                    case NORTH_STATION_SOUTH:
                        if (lastSensor == SensorName.CROSSING_NORTH) {
                            waitAtStation();
                        }
                        break;


                    //SOUTH STATION
                    case SOUTH_STATION_NORTH:
                        if (lastSensor == SensorName.SOUTH_JUNCTION_FROM_NORTH_EAST) {
                            waitAtStation();
                        }
                        break;
                    case SOUTH_STATION_SOUTH:
                        if (lastSensor == SensorName.SOUTH_JUNCTION_FROM_SOUTH_EAST) {
                            waitAtStation();
                        }
                        break;


                }


                lastSensor = sensorName;

            }

        }

        private void changeDirection() {
            direction *= -1;
        }

        public void waitAtStation() {
            try {
                tsi.setSpeed(id, 0);
                sleep(1000 + (20 * speed));
                changeDirection();
                tsi.setSpeed(id, this.direction * speed);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (CommandException e) {
                e.printStackTrace();
            }
        }
        

        public void run() {
            while (!this.isInterrupted()) {
                try {
                    handleSensorEvent(tsi.getSensor(this.id));
                } catch (CommandException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }
    }
}
