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
    private boolean indoParaCentro = false;
    private final double xMin, xMax, yMin, yMax;

    public Patrulhando(RoboTeste robo) {
        super(robo);
        int margem = robo.getSentryBorderSize() + (int) (robo.getWidth() / 2.0) + 3;
        xMin = margem;
        xMax = robo.getBattleFieldWidth() - margem;
        yMin = margem;
        yMax = robo.getBattleFieldHeight() - margem;
    }

    @Override
    public void run() {
        if (robo.getDistanceRemaining() != 0 || robo.getTurnRemaining() != 0) {
            return; // ainda terminando o movimento anterior
        }

        if (indoParaCentro) {
            robo.marcarCentroAlcancado();
            mudarEstado(new Procurando(robo));
            return;
        }

        if (ladosPercorridos >= 4) {
            irParaCentro();
            return;
        }

        if (!girandoNaBorda) {
            robo.setTurnRight(90);
            girandoNaBorda = true;
        } else {
            double lado = distanciaAteLimite();     
            robo.setAhead(lado);
            girandoNaBorda = false;
            ladosPercorridos++;
        }
    }

    private double distanciaAteLimite() {
        double heading = robo.getHeading();
        int quadrante = (int) Math.round(heading / 90.0) % 4;

        switch (quadrante) {
            case 0: return yMax - robo.getY();
            case 1: return xMax - robo.getX();
            case 2: return robo.getY() - yMin;
            default: return robo.getX() - xMin;
        }
    }

    private void irParaCentro() {
        double xCentro = robo.getBattleFieldWidth() / 2;
        double yCentro = robo.getBattleFieldHeight() / 2;
        double dx = xCentro - robo.getX();
        double dy = yCentro - robo.getY();
        double distanciaCentro = Math.hypot(dx, dy);

        double anguloCentro = Math.toDegrees(Math.atan2(dx, dy));
        robo.virarPara(anguloCentro);
        robo.setAhead(distanciaCentro);

        indoParaCentro = true; // só troca de estado quando esse movimento terminar
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
            robo.back(robo.getSentryBorderSize()); //aqui ele sai da SentryZone
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

class Atacando extends State {
    private double bearingAlvo;

    public Atacando(RoboTeste robo, double bearingAlvo) {
        super(robo);
        this.bearingAlvo = bearingAlvo;
    }

    public void atualizarAlvo(double bearingAlvo) {
        this.bearingAlvo = bearingAlvo;
    }

    @Override
    public void run() {
        robo.setTurnRadarLeft(360);

        double giroCanhao = robo.getHeading() + bearingAlvo - robo.getGunHeading();
        robo.setTurnGunRight(giroCanhao);
        robo.setFire(1);
    }

}

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

public class RoboTeste extends AdvancedRobot {
    private final Procurando procurando = new Procurando(this);
<<<<<<< Updated upstream
    private State estadoAtual = new SaindoZona(this);
    private double distanciaVolta;
    private double distanciaRobo;
    private boolean centroAlcancado;
=======
    private State estadoAtual = procurando;
    private int modo;
    private boolean centro, girar = true, recuar;
    private int lados;
    private double alvo, volta, xmin, xmax, ymin, ymax;
>>>>>>> Stashed changes

    void mudarEstado(State estado) {
        estadoAtual = estado;
    }

<<<<<<< Updated upstream
    void marcarCentroAlcancado() {
        centroAlcancado = true;
    }

    @Override
=======
>>>>>>> Stashed changes
    public void run() {
        setColors(Color.black, Color.white, Color.black);
        setAdjustRadarForGunTurn(true);
<<<<<<< Updated upstream

        while (true) {
            estadoAtual.run();
=======
        setAdjustGunForRobotTurn(true);
        while (true) {
            if (modo == 0) {
                procurando.run();
            } else if (modo == 1) {
                if (getDistanceRemaining() == 0) modo = 2;
            } else if (modo == 2) {
                if (getDistanceRemaining() == 0) {
                    if (!recuar) {
                        setBack(volta);
                        recuar = true;
                    } else {
                        iniciarPatrulha();
                    }
                }
            } else if (modo == 3) {
                patrulhar();
            } else if (modo == 5) {
                if (getDistanceRemaining() == 0 && getTurnRemaining() == 0) {
                    centro = true;
                    modo = 0;
                }
            } else {
                atacar();
            }
>>>>>>> Stashed changes
            execute();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) { 
        if (!procurando.encontrouRoboAlvo(e)) {
            return;
        }

        distanciaRobo = e.getDistance();

        if (estadoAtual instanceof Atacando) {
            ((Atacando) estadoAtual).atualizarAlvo(e.getBearing());
            return;
        }

        if (!(estadoAtual instanceof Procurando)) {
            return;
        }

        if (centroAlcancado) {
            estadoAtual = new Atacando(this, e.getBearing());
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
        //para que o robo fique na zona segura
        double margem = getSentryBorderSize();

        if (menor == distCima)
            virarPara(0);
        else if (menor == distDir)
            virarPara(90);
        else if (menor == distBaixo)
            virarPara(180);
        else
            virarPara(270);

        double distanciaAndar = menor - margemParede;
        distanciaVolta = margem;

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
        if (!centroAlcancado) {
            return;
        } else {
            setBack(distanciaRobo + 30);
            mudarEstado(new Atacando(this, e.getBearing()));
        }

    }

    void virarPara(double anguloAbsoluto) {
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