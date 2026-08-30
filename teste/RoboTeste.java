package teste;

import robocode.*;
import java.awt.Color;

public class RoboTeste extends AdvancedRobot {
    private enum Estado { PROCURANDO, INDO_PAREDE, SAINDO_ZONA, FINALIZADO }
    
    private Estado estadoAtual = Estado.PROCURANDO;
    private double distanciaVolta = 0;
    private final String alissonName = "sample.Target"; // por enquanto

    @Override
    public void run() {
        setColors(Color.black, Color.red, Color.black);
        setAdjustRadarForGunTurn(true);

        while (true) {
            switch (estadoAtual) {
                case PROCURANDO:
                    setTurnRadarRight(360);
                    break;
                    
                case INDO_PAREDE:
                    if (getDistanceRemaining() == 0) {
                        estadoAtual = Estado.SAINDO_ZONA;
                        setBack(distanciaVolta);
                    }
                    break;
                    
                case SAINDO_ZONA:
                    if (getDistanceRemaining() == 0) {
                        estadoAtual = Estado.FINALIZADO;
                    }
                    break;
                    
                case FINALIZADO:
                    break;
            }
            execute();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        if (estadoAtual != Estado.PROCURANDO || !e.getName().contains(alissonName)) {
            return;
        }

        double x = getX();
        double y = getY();
        double largura = getBattleFieldWidth();
        double altura = getBattleFieldHeight();

        double distCima = altura - y;
        double distDir = largura - x;
        double distBaixo = y;
        double distEsq = x;

        double menor = Math.min(Math.min(distCima, distBaixo), Math.min(distEsq, distDir));

        double margemParede = (getWidth() / 2.0) + 3;
        double margemSegura = getSentryBorderSize() + 20;

        if (menor == distCima) virarPara(0);
        else if (menor == distDir) virarPara(90);
        else if (menor == distBaixo) virarPara(180);
        else virarPara(270);

        double distanciaAndar = menor - margemParede;
        distanciaVolta = margemSegura - margemParede;

        if (distanciaAndar > 0) {
            setAhead(distanciaAndar);
            estadoAtual = Estado.INDO_PAREDE; 
        } else {
            setBack(distanciaVolta);
            estadoAtual = Estado.SAINDO_ZONA;
        }
    }

    @Override
    public void onHitRobot(HitRobotEvent e) {
        if (estadoAtual == Estado.INDO_PAREDE) {
            setAhead(0); 
            estadoAtual = Estado.SAINDO_ZONA; 
            
            setBack(distanciaVolta + 40); 
        }
    }

    private void virarPara(double anguloAbsoluto) {
        double anguloVirar = normalizeBearing(anguloAbsoluto - getHeading());
        turnRight(anguloVirar);
    }

    private double normalizeBearing(double angle) {
        angle = angle % 360;
        if (angle > 180) angle -= 360;
        if (angle < -180) angle += 360;
        return angle;
    }

    @Override
    public void onHitWall(HitWallEvent e) {}
    @Override
    public void onHitByBullet(HitByBulletEvent e) {}
}