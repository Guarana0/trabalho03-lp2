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
    public Procurando(RoboTeste robo) {
        super(robo);
    }

    @Override
    public void run() {
        robo.setTurnRadarLeft(360);
    }

    public boolean encontrouRoboAlvo(ScannedRobotEvent e) {
        return e.getName().toLowerCase().contains("alisson");
    }
}

class Patrulhando extends State {
    private boolean girando = true;
    private int ladosPercorridos;
    private boolean indoParaCentro;
    private final double xMin, xMax, yMin, yMax;

    public Patrulhando(RoboTeste robo) {
        super(robo);
        int margem = robo.getSentryBorderSize() + 21;
        xMin = margem;
        xMax = robo.getBattleFieldWidth() - margem;
        yMin = margem;
        yMax = robo.getBattleFieldHeight() - margem;
    }

    @Override
    public void run() {
        if (robo.getDistanceRemaining() != 0 || robo.getTurnRemaining() != 0) {
            return;
        }

        if (indoParaCentro) {
            robo.marcarCentroAlcancado();
            mudarEstado(new Procurando(robo));
            return;
        }

        if (girando) {
            robo.setTurnRight(90);
            girando = false;
            return;
        }

        double lado = distanciaAteLimite();
        if (lado > 1) {
            robo.setAhead(lado);
            return;
        }

        ladosPercorridos++;
        girando = true;
        if (ladosPercorridos > 4) {
            irParaCentro();
        }
    }

    private double distanciaAteLimite() {
        int quadrante = (int) Math.round(robo.getHeading() / 90.0) % 4;
        switch (quadrante) {
            case 0: return yMax - robo.getY();
            case 1: return xMax - robo.getX();
            case 2: return robo.getY() - yMin;
            default: return robo.getX() - xMin;
        }
    }

    private void irParaCentro() {
        double dx = robo.getBattleFieldWidth() / 2 - robo.getX();
        double dy = robo.getBattleFieldHeight() / 2 - robo.getY();
        robo.virarPara(Math.toDegrees(Math.atan2(dx, dy)));
        robo.setAhead(Math.hypot(dx, dy));
        indoParaCentro = true;
    }
}

class SaindoZona extends State {
    private final double distanciaVolta;
    private boolean recuou;

    public SaindoZona(RoboTeste robo) {
        super(robo);
        distanciaVolta = robo.getSentryBorderSize();
    }

    @Override
    public void run() {
        if (robo.getDistanceRemaining() == 0) {
            if (!recuou) {
                robo.setBack(distanciaVolta);
                recuou = true;
            } else {
                mudarEstado(new Patrulhando(robo));
            }
        }
    }
}

public class RoboTeste extends AdvancedRobot {
    private final Procurando procurando = new Procurando(this);
    private State estadoAtual = procurando;
    private boolean centroAlcancado;
    private boolean alissonMorto;

    void mudarEstado(State estado) {
        estadoAtual = estado;
    }

    void marcarCentroAlcancado() {
        centroAlcancado = true;
    }

    @Override
    public void run() {
        setColors(Color.black, Color.white, Color.black);
        setAdjustRadarForGunTurn(true);
        setAdjustGunForRobotTurn(true);

        while (true) {
            estadoAtual.run();
            execute();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        if (!procurando.encontrouRoboAlvo(e) && !alissonMorto) {
            return;
        }

        if (!(estadoAtual instanceof Procurando)) {
            return;
        }

        if (centroAlcancado) {
            estadoAtual = new Patrulhando(this);
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

        if (menor == distCima) {
            virarPara(0);
        } else if (menor == distDir) {
            virarPara(90);
        } else if (menor == distBaixo) {
            virarPara(180);
        } else {
            virarPara(270);
        }

        double distanciaAndar = menor - 21;
        if (distanciaAndar > 0) {
            setAhead(distanciaAndar);
        } else {
            setBack(getSentryBorderSize());
        }

        estadoAtual = new SaindoZona(this);
    }

    @Override
    public void onRobotDeath(RobotDeathEvent e) {
        if (e.getName().toLowerCase().contains("alisson")) {
            alissonMorto = true;
            estadoAtual = procurando;
        }
    }

    void virarPara(double anguloAbsoluto) {
        turnRight(normalizeBearing(anguloAbsoluto - getHeading()));
    }

    private double normalizeBearing(double angle) {
        angle %= 360;
        if (angle > 180) angle -= 360;
        if (angle < -180) angle += 360;
        return angle;
    }
}