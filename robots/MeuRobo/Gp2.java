package MeuRobo;

import robocode.*;
import robocode.util.Utils;

abstract class State {
    Gp2 robo;
    State(Gp2 robo) {
        this.robo = robo;
    }
    abstract void run();
}

class Procurando extends State {
    Procurando(Gp2 robo) {
        super(robo);
    }
    void run() {
        robo.setTurnRadarLeft(360);
    }
}

class Patrulhando extends State {
    private boolean girando = true, girandoParaCentro, indoParaCentro;
    private int lados;

    Patrulhando(Gp2 robo) {
        super(robo);
    }

    void run() {
        if (robo.getDistanceRemaining() != 0 || robo.getTurnRemaining() != 0) {
            return;
        }
        double w = robo.getBattleFieldWidth(), h = robo.getBattleFieldHeight();
        double x = robo.getX(), y = robo.getY();

        if (indoParaCentro) {
            robo.centroAlcancado = true;
            robo.estadoAtual = robo.procurando;
            return;
        }

        if (girandoParaCentro) {
            robo.setAhead(Math.hypot(w / 2 - x, h / 2 - y));
            indoParaCentro = true;
            return;
        }

        if (girando) {
            robo.setTurnRight(90);
            girando = false;
            return;
        }

        double margem = robo.getSentryBorderSize() + 21;
        int q = (int) ((robo.getHeading() + 45) / 90) % 4;
        double lado = q == 0 ? h - margem - y : q == 1 ? w - margem - x : q == 2 ? y - margem : x - margem;

        if (lado > 1) {
            robo.setAhead(lado);
            return;
        }

        if (++lados > 4) {
            robo.setTurnRight(Utils.normalRelativeAngleDegrees(Math.toDegrees(Math.atan2(w / 2 - x, h / 2 - y)) - robo.getHeading()));
            girandoParaCentro = true;
        } else {
            girando = true;
        }
    }
}

class SaindoZona extends State {
    private boolean recuou;

    SaindoZona(Gp2 robo) {
        super(robo);
    }

    void run() {
        if (robo.getDistanceRemaining() == 0) {
            if (!recuou) {
                robo.setBack(robo.getSentryBorderSize());
                recuou = true;
            } else {
                robo.estadoAtual = new Patrulhando(robo);
            }
        }
    }
}

class Atacando extends State {
    double bearingAlvo;

    Atacando(Gp2 robo, double bearingAlvo) {
        super(robo);
        this.bearingAlvo = bearingAlvo;
    }

    void atualizarAlvo(ScannedRobotEvent evento) {
        double distancia = evento.getDistance();
        double velocidadeTiro = Math.abs(evento.getVelocity()) + 20;
        double tempo = distancia / velocidadeTiro;
        double anguloInimigo = Math.toRadians(evento.getHeading());
        double bearingAbsoluto = evento.getBearing() + robo.getHeading();
        double anguloAtual = Math.toRadians(bearingAbsoluto);
        double alvoX = robo.getX() + Math.sin(anguloAtual) * distancia;
        double alvoY = robo.getY() + Math.cos(anguloAtual) * distancia;
        double futuroX = alvoX + Math.sin(anguloInimigo) * evento.getVelocity() * tempo;
        double futuroY = alvoY + Math.cos(anguloInimigo) * evento.getVelocity() * tempo;

        bearingAlvo = Math.toDegrees(Math.atan2(futuroX - robo.getX(), futuroY - robo.getY()));
    }

    void run() {
        robo.setTurnRadarRight(robo.alissonNaPartida && !robo.alissonMorto ? 
            2 * Utils.normalRelativeAngleDegrees(bearingAlvo - robo.getRadarHeading()) : 360);

        double dx = robo.getBattleFieldWidth() / 2 - robo.getX();
        double dy = robo.getBattleFieldHeight() / 2 - robo.getY();

        if (Math.hypot(dx, dy) > robo.getBattleFieldHeight() / 2 - robo.getSentryBorderSize() - 100) {
            robo.setTurnRight(Utils.normalRelativeAngleDegrees(Math.toDegrees(Math.atan2(dx, dy)) - robo.getHeading()));
            robo.setAhead(99);
        } else {
            robo.setAhead(Math.sin(robo.getTime() / 15.0) * 40);
            robo.setTurnRight(Utils.normalRelativeAngleDegrees(bearingAlvo - robo.getHeading() + 90));
        }

        robo.setTurnGunRight(Utils.normalRelativeAngleDegrees(bearingAlvo - robo.getGunHeading()));
        robo.setFire(1);
    }
}

public class Gp2 extends AdvancedRobot {
    final Procurando procurando = new Procurando(this);
    State estadoAtual = procurando;
    boolean centroAlcancado, alissonMorto, alissonNaPartida;

    @Override
    public void run() {
        setAdjustRadarForGunTurn(true);
        setAdjustGunForRobotTurn(true);
        while (true) {
            estadoAtual.run();
            execute();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        boolean eAlisson = e.getName().toLowerCase().contains("alisson");
        if (eAlisson) {
            alissonNaPartida = true;
        }
        if (alissonNaPartida && !alissonMorto && !eAlisson) {
            return;
        }
        double bearingAbsoluto = e.getBearing() + getHeading();

        if (eAlisson && !centroAlcancado) {
            if (!(estadoAtual instanceof SaindoZona) && !(estadoAtual instanceof Patrulhando)) {
                estadoAtual = new SaindoZona(this);
                double x = getX(), y = getY(), w = getBattleFieldWidth(), h = getBattleFieldHeight();
                double menor = Math.min(Math.min(h - y, y), Math.min(x, w - x));

                setTurnRight(Utils.normalRelativeAngleDegrees((menor == h - y ? 0 : menor == w - x ? 90 : menor == y ? 180 : 270) - getHeading()));

                if (menor > 21) {
                    setAhead(menor - 21);
                } else {
                    setBack(getSentryBorderSize());
                }
            }
            return;
        }

        if (estadoAtual instanceof Atacando) {
            ((Atacando) estadoAtual).atualizarAlvo(e);
        } else {
            estadoAtual = new Atacando(this, bearingAbsoluto);
            ((Atacando) estadoAtual).atualizarAlvo(e);
        }

        if(e.getName().equals("BorderGuard")){
            return;
        }
    }

    @Override
    public void onRobotDeath(RobotDeathEvent e) {
        if (e.getName().toLowerCase().contains("alisson")) {
            alissonMorto = true;
        }
        estadoAtual = procurando;
    }

    @Override
    public void onHitRobot(HitRobotEvent event) {
        if(estadoAtual == procurando){
            back(45);
            setTurnLeft(45);
            back(45);
        }
    }
}