package MeuRobo;

import robocode.*;
import java.awt.Color;

abstract class State {
    protected RoboTeste robo;

    public State(RoboTeste robo) {
        this.robo = robo;
    }

    public abstract void run();

    protected void mudarEstado(State estado) {
        robo.mudarEstado(estado);
    }
}

class Procurando extends State {
    private final String roboAlisson = "MeuRobo.alisson";
    private boolean achouAlisson = false;

    public Procurando(RoboTeste robo) {
        super(robo);
    }

    @Override
    public void run() {

        robo.setTurnRadarLeft(360);
    }

    public boolean encontrouRoboAlvo(ScannedRobotEvent e) {
        if (!e.getName().contains(roboAlisson)) {
            return false;
        } else {
            achouAlisson = true;
            return true;
        }
    }

}

class Patrulhando extends State {
    private boolean girandoNaBorda = false;
    private int ladosPercorridos = 0;

    public Patrulhando(RoboTeste robo) {
        super(robo);
    }

    @Override
    public void run() {
        // aqui agente confere se o robonao ta fazendo nada
        if (robo.getDistanceRemaining() == 0 && robo.getTurnRemaining() == 0) {
            // conferimos quantos lados ele percorreu
            if (ladosPercorridos >= 5) {
                if (!girandoNaBorda) {
                    robo.setTurnRight(135);
                    girandoNaBorda = true;
                } else {
                    double xCentro = robo.getBattleFieldHeight() / 2;
                    double yCentro = robo.getBattleFieldWidth() / 2;
                    double distanciaCentro = Math.max(xCentro, yCentro);

                    robo.setAhead(distanciaCentro);
                    mudarEstado(new Procurando(robo));
                }
                return;
            }

            if (!girandoNaBorda) {
                robo.setTurnRight(90);
                girandoNaBorda = true;
            } else {
                double tamanhoTrecho = Math.max(robo.getBattleFieldWidth(), robo.getBattleFieldHeight());
                robo.setAhead(tamanhoTrecho);
                girandoNaBorda = false;
                ladosPercorridos++;
            }
        }
    }
}

class Finalizado extends State {
    public Finalizado(RoboTeste robo) {
        super(robo);
    }

    @Override
    public void run() {
        return;
    }
}

class SaindoZona extends State {
    public SaindoZona(RoboTeste robo) {
        super(robo);
    }

    @Override
    public void run() {
        if (robo.getDistanceRemaining() == 0) {
            mudarEstado(new Patrulhando(robo));
        }
    }
}

class IndoParede extends State {
    private final double distanciaVolta;

    public IndoParede(RoboTeste robo) {
        super(robo);
        double margemParede = (robo.getWidth() / 2.0) + 3;
        double margemSegura = robo.getSentryBorderSize() + 20;
        distanciaVolta = margemSegura - margemParede;
    }

    @Override
    public void run() {
        if (robo.getDistanceRemaining() == 0) {
            mudarEstado(new SaindoZona(robo));
        }
    }
}

public class RoboTeste extends AdvancedRobot {
    private final Procurando procurando = new Procurando(this);
    private State estadoAtual = procurando;
    private double distanciaVolta;

    void mudarEstado(State estado) {
        estadoAtual = estado;
    }

    @Override
    public void run() {
        setColors(Color.black, Color.red, Color.black);
        setAdjustRadarForGunTurn(true);

        while (true) {
            estadoAtual.run();
            execute();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        if (!(estadoAtual instanceof Procurando) || !procurando.encontrouRoboAlvo(e)) {
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

        if (menor == distCima)
            virarPara(0);
        else if (menor == distDir)
            virarPara(90);
        else if (menor == distBaixo)
            virarPara(180);
        else
            virarPara(270);

        double distanciaAndar = menor - margemParede;
        distanciaVolta = margemSegura - margemParede;

        if (distanciaAndar > 0) {
            setAhead(distanciaAndar);
            estadoAtual = new IndoParede(this);
        } else {
            setBack(distanciaVolta);
            estadoAtual = new SaindoZona(this);
        }
    }

    @Override
    public void onHitRobot(HitRobotEvent e) {
        if (estadoAtual instanceof IndoParede) {
            setAhead(0);
            estadoAtual = new SaindoZona(this);

            setBack(distanciaVolta + 40);
        }
    }

    private void virarPara(double anguloAbsoluto) {
        double anguloVirar = normalizeBearing(anguloAbsoluto - getHeading());
        turnRight(anguloVirar);
    }

    private double normalizeBearing(double angle) {
        angle = angle % 360;
        if (angle > 180)
            angle -= 360;
        if (angle < -180)
            angle += 360;
        return angle;
    }

    @Override
    public void onHitWall(HitWallEvent e) {
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
    }
}