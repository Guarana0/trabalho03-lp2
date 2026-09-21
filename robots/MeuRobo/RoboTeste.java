package MeuRobo;

import robocode.*;

public class RoboTeste extends AdvancedRobot {
    private int modo;
    private boolean centro, girar = true, recuar;
    private int lados;
    private double alvo, volta, xmin, xmax, ymin, ymax;

    public void run() {
        setAdjustRadarForGunTurn(true);
        while (true) {
            if (modo == 0) {
                setTurnRadarLeft(360);
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
            execute();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        if (!e.getName().toLowerCase().contains("alisson")) return;
        alvo = e.getBearing();
        if (modo == 4) return;
        if (modo != 0) return;
        if (centro) {
            modo = 4;
            return;
        }

        double x = getX(), y = getY();
        double w = getBattleFieldWidth(), h = getBattleFieldHeight();
        double cima = h - y, direita = w - x, baixo = y, esquerda = x;
        double menor = Math.min(Math.min(cima, baixo), Math.min(esquerda, direita));
        double margem = getWidth() / 2 + 3;
        if (menor == cima) virar(0);
        else if (menor == direita) virar(90);
        else if (menor == baixo) virar(180);
        else virar(270);
        double distancia = menor - margem;
        volta = getSentryBorderSize();
        if (distancia > 0) {
            setAhead(distancia);
            modo = 1;
        } else {
            setBack(volta);
            recuar = true;
            modo = 2;
        }
    }

    private void patrulhar() {
        if (getDistanceRemaining() != 0 || getTurnRemaining() != 0) return;
        if (girar) {
            setTurnRight(90);
            girar = false;
            return;
        }
        int q = (int) Math.round(getHeading() / 90) % 4;
        double d = q == 0 ? ymax - getY() : q == 1 ? xmax - getX() : q == 2 ? getY() - ymin : getX() - xmin;
        if (d > 1) {
            setAhead(d);
            return;
        }
        girar = true;
        if (++lados < 4) return;
        double dx = getBattleFieldWidth() / 2 - getX();
        double dy = getBattleFieldHeight() / 2 - getY();
        virar(Math.toDegrees(Math.atan2(dx, dy)));
        setAhead(Math.hypot(dx, dy));
        modo = 5;
    }

    private void iniciarPatrulha() {
        int m = getSentryBorderSize() + (int) (getWidth() / 2) + 3;
        xmin = m;
        xmax = getBattleFieldWidth() - m;
        ymin = m;
        ymax = getBattleFieldHeight() - m;
        girar = true;
        lados = 0;
        modo = 3;
    }

    private void atacar() {
        setTurnRadarLeft(360);
        double giro = normalizar(getHeading() + alvo - getGunHeading());
        setTurnGunRight(giro);
        if (Math.abs(giro) < 3) setFire(2);
    }

    public void onRobotDeath(RobotDeathEvent e) {
        if (e.getName().toLowerCase().contains("alisson")) {
            modo = 0;
        }
    }

    private void virar(double angulo) {
        setTurnRight(normalizar(angulo - getHeading()));
    }

    private double normalizar(double angulo) {
        angulo %= 360;
        if (angulo > 180) angulo -= 360;
        if (angulo < -180) angulo += 360;
        return angulo;
    }
}
