import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;


public class Window extends JFrame {
    private static Window WINDOW = null;
    private Dimension SCREENSIZE = Toolkit.getDefaultToolkit().getScreenSize();
    private JList list;
    private JSlider slider;
    private JPanel drawPanel;
    private Vector<int[]> points = new Vector<>(2);
    private Vector<int[]> rotatedPoints = new Vector<>();
    private Vector<Integer> rotateCount = new Vector<>();
    private int zoomForCantor = 0;
    private JSlider smoothSlider;
    private BufferedImage image;

    private boolean isRotated = false;
    private int currentIteration = 0;
    private boolean leftButtomPressed = false;
    private int lastX;
    private int lastY;
    private int lastForm = -1;  //form of fractal


    private Window() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1400, 900);
        this.setLocation(SCREENSIZE.width / 2 - this.getSize().width / 2,
                SCREENSIZE.height / 2 - this.getSize().height / 2);
        getContentPane().setLayout(new MigLayout("", "[75sp,grow][50sp,grow]", "[5sp:5sp,grow][90sp,grow]"));

        String[] types = {"Кривая Коха", "Кривая Леви", "Кривая Минковского", "Кривая Гильберта", "Кривая дракона",
                "Кривая Пеано", "Треугольник Серпинского", "Множество Кантора"};

        JScrollPane scrollPane = new JScrollPane();
        getContentPane().add(scrollPane, "cell 0 0,grow");
        list = new JList(types);
        scrollPane.setViewportView(list);


        JPanel buttonsPanel = new JPanel();
        getContentPane().add(buttonsPanel, "cell 1 0,grow");

        JLabel iterationLabel = new JLabel("1");
        buttonsPanel.add(iterationLabel);

        slider = new JSlider(1, 9);
        slider.setValue(1);
        buttonsPanel.add(slider);
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                iterationLabel.setText(String.valueOf(slider.getValue()));
            }
        });

        JButton calculationButton = new JButton("Построить");
        buttonsPanel.add(calculationButton);
        calculationButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                paintFractal();
            }
        });
        JButton exportButton = new JButton("Экспорт");
        exportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        });
        buttonsPanel.add(exportButton);
        drawPanel = new JPanel();
        drawPanel.setPreferredSize(new Dimension((int) (this.getWidth() * 0.85), (int) (this.getHeight() * 0.9)));
        drawPanel.setSize((int) (this.getWidth() * 0.7), (int) (this.getHeight() * 0.7));
        clearAndInit();
        getContentPane().add(drawPanel, "cell 0 1");

        JPanel panel = new JPanel();
        getContentPane().add(panel, "cell 1 1,grow");
        drawPanel.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                boolean isFounded = false;
                int x = 0;
                int y = 0;
                int mX = 85;
                int mY = 405;
                for(int[] i : points){
                    if(e.getX()>=i[0]+mX-5 & e.getX()<=i[0]+mX+5 & e.getY()+34>=i[1]+mY-5 & e.getY()+34<=i[1]+mY+5){
                        isFounded = true;
                        x = i[0];
                        y = i[1];
                    }
                }
                if (isFounded){
                    if (e.getPreciseWheelRotation() == 1) rotateFractal(x,y,1);
                    else rotateFractal(x,y,-1);
                }
                else{
                    if (e.getPreciseWheelRotation() == 1) scaleFractal(e.getX()-90,e.getY()-400,1);
                    else scaleFractal(e.getX()-90,e.getY()-400,-1);
                }
            }
        });

        drawPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                leftButtomPressed = true;
                lastX = e.getX();
                lastY = e.getY();
            }
            @Override
            public void mouseReleased(MouseEvent e)
            {
                leftButtomPressed = false;
            }
        });
        drawPanel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int x = 0;
                int y = 0;
                if (leftButtomPressed){
                    if(e.getX()>lastX) x = 1;
                    if(e.getX()<lastX) x = -1;
                    if(e.getY()>lastY) y = 1;
                    if(e.getY()<lastY) y = -1;
                    moveFractal(x,y);
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {

            }
        });

        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                 if(points.size()!=0){
                     FileWritter.save(image);
                 }
            }
        });

        JLabel smoothLabel = new JLabel("Уровень сглаживания: 0");
        panel.add(smoothLabel);

        smoothSlider = new JSlider(0,3,0);
        smoothSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                smoothLabel.setText("Уровень сглаживания: " + smoothSlider.getValue());
            }
        });
        panel.add(smoothSlider);
        this.setVisible(true);
    }

    public static Window getInstance() {
        if (WINDOW == null) WINDOW = new Window();
        return WINDOW;
    }
    private void clearAndInit(){
        points.clear();
        points.add(new int[]{0, 0, 666, 0});
    }
    private void onlyClear(){
        points.clear();
    }
    private void clearAndInitHilbert(){
        points.clear();
        int h = drawPanel.getWidth();
        int x = 150;
        int y = 467;
        points.add(new int[]{h/3-x,h/3-y,h/3-x,h/3*2-y});
        points.add(new int[]{h/3-x,h/3*2-y,h/3*2-x,h/3*2-y});
        points.add(new int[]{h/3*2-x,h/3*2-y,h/3*2-x,h/3-y});
    }
    private void clearAndInitSierpinsky(){
        points.clear();
        points.add(new int[]{0,0,333,-333,666,0});
    }
    private void clearAndInitCantor(){
        points.clear();
        currentIteration = 0;
        points.add(new int[]{0,0,666});
    }

    private void moveFractal(int x, int y){
        if (list.getSelectedIndex() !=6 & list.getSelectedIndex()!=7){
            for (int[] i : points){
                i[0]+=x;
                i[1]+=y;
                i[2]+=x;
                i[3]+=y;
            }
        }
        else if(list.getSelectedIndex()==6){
            for (int[] i : points){
                i[0]+=x;
                i[1]+=y;
                i[2]+=x;
                i[3]+=y;
                i[4]+=x;
                i[5]+=y;
            }
        }
        else if(list.getSelectedIndex() == 7){
            for (int[] i : points){
                i[0]+=x;
                i[1]+=y;
            }
        }
        paintFractal();
    }

    public void paintFractal() {
        if(lastForm==list.getSelectedIndex()){
            if(isRotated) points = rotatedPoints;
            if(slider.getValue()<currentIteration){
                switch (lastForm){
                    case 0: decreaseKoch(currentIteration-slider.getValue()); break;
                    case 1: decreaseLevy(currentIteration-slider.getValue()); break;
                    case 2: decreaseMinkowski(currentIteration - slider.getValue()); break;
                    case 4: decreaseDragon(currentIteration - slider.getValue()); break;
                    case 5: onlyClear(); calculatePeano(slider.getValue()); break;
                    case 6: clearAndInitSierpinsky(); calculateSierpinsky(slider.getValue()); break;
                    case 7: clearAndInitCantor(); calculateCantor(slider.getValue()); break;

                }
            }
            else if(slider.getValue()>currentIteration){
                switch (lastForm){
                    case 0: calculateKoch(slider.getValue() - currentIteration); break;
                    case 1: calculateLevy(slider.getValue() - currentIteration); break;
                    case 2: calculateMinkowski(slider.getValue() - currentIteration); break;
                    case 4: calculateDragon(slider.getValue() - currentIteration); break;
                    case 5: onlyClear(); calculatePeano(slider.getValue()); break;
                    case 6: calculateSierpinsky(slider.getValue() - currentIteration); break;
                    case 7: calculateCantor( slider.getValue() - currentIteration); break;
                }
            }
        }
        else {
            currentIteration=0;
            clearAndInit();
            int c = slider.getValue();
            switch (list.getSelectedIndex()){
                case 0: calculateKoch(c); break;
                case 1: calculateLevy(c); break;
                case 2: calculateMinkowski(c); break;
                case 3:
                    clearAndInitHilbert();
                    calculateHilbert(c);
                        break;
                case 4:
                    calculateDragon(c); break;
                case 5:
                    onlyClear();
                    calculatePeano(c);
                    break;
                case 6:
                    clearAndInitSierpinsky();
                    calculateSierpinsky(c);
                    break;
                case 7:
                    clearAndInitCantor();
                    calculateCantor(c);
            }
        }
        image =  new BufferedImage(drawPanel.getHeight(), drawPanel.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics g = this.getGraphics();
        Graphics g1 = image.getGraphics();

        g.setClip(0,106,drawPanel.getHeight(),drawPanel.getHeight());
        g.setColor(Color.white);
        g.fillRect(drawPanel.getX(),drawPanel.getY()+40,drawPanel.getWidth(),drawPanel.getHeight());
        g1.setClip(7,106,drawPanel.getHeight(),drawPanel.getHeight());
        g1.setColor(Color.white);
        g1.fillRect(drawPanel.getX(),drawPanel.getY()+40,drawPanel.getWidth(),drawPanel.getHeight());
        int mX = 100;
        int mY = 467;
        g.setColor(Color.BLACK);
        g1.setColor(Color.BLACK);
        Vector<int[]> temp;
        if(isRotated) temp = rotatedPoints;
        else temp = points;
        if (list.getSelectedIndex()!=6 & list.getSelectedIndex() !=7){
            for(int[] i : temp){
                g1.drawLine(i[0] + mX, i[1] + mY, i[2] + mX, i[3] + mY);
                g.drawLine(i[0] + mX, i[1] + mY, i[2] + mX, i[3] + mY);
            }
        }
        if(list.getSelectedIndex() == 6){
            for (int[] i : temp){
                System.out.println(i[0] + " " + i[1] + "   " + i[2] + " " + i[3] + "   " + i[4] + " " + i[5]);
                int[] xp = new int[]{i[0],i[2],i[4]};
                int[] yp = new int[]{i[1]+500, i[3]+500, i[5]+500};
                g1.fillPolygon(xp,yp,3);
                g.fillPolygon(xp,yp,3);
            }
        }
        if (list.getSelectedIndex() == 7){
            for (int[] i : temp){
                g1.fillRect(i[0] + mX,i[1]+mY,i[2],10);
                g.fillRect(i[0] + mX,i[1]+mY,i[2],10);
            }
        }
    }

    private void rotateFractal(int axisX, int axisY, int multiplier){
        if(list.getSelectedIndex()!=5){
            double cos = 0.98480775301;
            double sin = 0.17364817766 * multiplier;
            Vector<int[]> newPoints = new Vector<>(points.size());
            Vector<int []> temp;
            if(isRotated) temp = rotatedPoints;
            else temp = points;
            for (int[] i : temp){
                int i0 = (int) (cos * (i[0] - axisX) + sin * (i[1] - axisY)) + axisX;
                int i1 = (int) (cos * (i[1] - axisY) - sin * (i[0] - axisX)) + axisY;
                int i2 = (int) (cos * (i[2] - axisX) + sin * (i[3] - axisY)) + axisX;
                int i3 = (int) (cos * (i[3] - axisY) - sin * (i[2] - axisX)) + axisY;
                newPoints.add(new int[]{i0,i1,i2,i3});
            }
            rotatedPoints = newPoints;
            isRotated = true;
            paintFractal();
        }
    }

    private void scaleFractal(int axisX,int axisY,int type){
        if(list.getSelectedIndex()!=7 & list.getSelectedIndex() !=6){
            if(type==-1){
                for (int[] i : points){
                    i[0]= axisX + (i[0]-axisX)*2;
                    i[1]= axisY + (i[1]-axisY)*2;
                    i[2]= axisX + (i[2]-axisX)*2;
                    i[3]= axisY + (i[3]-axisY)*2;
                }
            }
            else {
                for (int[] i : points){
                    i[0]= (int) (axisX + (i[0]-axisX)*0.5);
                    i[1]= (int) (axisY + (i[1]-axisY)*0.5);
                    i[2]= (int) (axisX + (i[2]-axisX)*0.5);
                    i[3]= (int) (axisY + (i[3]-axisY)*0.5);
                }
            }
        }
        else if(list.getSelectedIndex()!=6){
            if(type==-1){
                for (int[] i : points){
                    i[0]= axisX + (i[0]-axisX)*2;
                    i[1]= axisY + (i[1]-axisY)*2;
                    i[2]= i[2]*2;
                }
            }
            else {
                for (int[] i : points){
                    i[0]= (int) (axisX + (i[0]-axisX)*0.5);
                    i[1]= (int) (axisY + (i[1]-axisY)*0.5);
                    i[2]= i[2]/2;
                }
            }
        }
        else {
            if(type==-1){
                for (int[] i : points){
                    i[0]= axisX + (i[0]-axisX)*2;
                    i[1]= axisY + (i[1]-axisY)*2;
                    i[2]= axisX + (i[2]-axisX)*2;
                    i[3]= axisY + (i[3]-axisY)*2;
                    i[4]= axisX + (i[4]-axisX)*2;
                    i[5]= axisY + (i[5]-axisY)*2;
                }
            }
            else {
                for (int[] i : points){
                    i[0]= (int) (axisX + (i[0]-axisX)*0.5);
                    i[1]= (int) (axisY + (i[1]-axisY)*0.5);
                    i[2]= (int) (axisX + (i[2]-axisX)*0.5);
                    i[3]= (int) (axisY + (i[3]-axisY)*0.5);
                    i[4]= (int) (axisX + (i[4]-axisX)*0.5);
                    i[5]= (int) (axisY + (i[5]-axisY)*0.5);
                }
            }
        }
        paintFractal();
    }

    private void smooth(){
        //                                D
        //                               |
        //           D                  N
        //         /                  /
        //    A---B               A--M
        int xa;
        int ya;
        int xd;
        int yd;
        int xb;
        int yb;
        int xm;
        int xn;
        for (int i = 0;i<points.size()-1;i++){
            xa = points.get(i)[0];
            ya = points.get(i)[1];
            xb = points.get(i)[2];
            yb = points.get(i)[3];
            xd = points.get(i+1)[2];
            yd = points.get(i+1)[3];
        }
    }

    private void calculateKoch(int count) {
        int xa, ya, xb, yb, xc, yc, xd, yd ,xe ,ye;
        double cos60 = 0.5;
        double sin60 = -0.866;
        for (int i = 0; i < count; i++) {
            Vector<int[]> temp = new Vector<>(i^5);
            for (int[] t : points){
                xa = t[0];
                ya = t[1];
                xe = t[2];
                ye = t[3];
                xb = (int)(xa + (xe - xa)*0.33);
                yb = (int)(ya + (ye - ya)*0.33);
                xd = (int)(xa + (xe - xa)*0.66);
                yd = (int)(ya + (ye - ya)*0.66);
                xc = (int)(xb + (xd - xb)*cos60 - sin60 * (yd-yb));
                yc = (int)(yb + (xd - xb)*sin60 + cos60 * (yd-yb));
                temp.add(new int[]{xa,ya,xb,yb});
                temp.add(new int[]{xb,yb,xc,yc});
                temp.add(new int[]{xc,yc,xd,yd});
                temp.add(new int[]{xd,yd,xe,ye});
            }
            points = temp;
            if(isRotated) rotatedPoints = temp;
        }
        currentIteration = slider.getValue();
        lastForm = 0;
    }
    private void decreaseKoch(int k){
        Vector<int[]> t1;
        while (k>0){
            if(isRotated) t1 = rotatedPoints;
            else t1 = points;
            Vector<int[]> temp = new Vector<>(t1.size()/5);
            for(int i = 0;i<t1.size()-3;i+=4){
                int xa = t1.get(i)[0];
                int ya = t1.get(i)[1];
                int xb = t1.get(i+3)[2];
                int yb = t1.get(i+3)[3];
                int[] t = new int[]{xa,ya,xb,yb};
                temp.add(t);
            }
            if(isRotated) rotatedPoints = temp;
            else points = temp;
            k--;
        }
        currentIteration = slider.getValue();
    }
    private void calculateLevy(int count){
        int xa;
        int ya;
        int xb;
        int yb;
        int xc;
        int yc;
        for (int i = 0; i < count; i++) {
            Vector<int[]> temp = new Vector<>(i*2);
            for (int[] t : points){
                xa = t[0];
                ya = t[1];
                xc = t[2];
                yc = t[3];
                xb = (xa + xc) / 2 + (yc - ya) / 2;
                yb = (ya + yc) / 2 - (xc - xa) / 2;
                temp.add(new int[]{xa,ya,xb,yb});
                temp.add(new int[]{xb,yb,xc,yc});
            }
            points = temp;
            if(isRotated) rotatedPoints = temp;
        }
        currentIteration = slider.getValue();
        lastForm = 1;
    }
    private void decreaseLevy(int k){
        Vector<int[]> t1;
        while (k>0){
            if(isRotated) t1 = rotatedPoints;
            else t1 = points;
            Vector<int[]> temp = new Vector<>(t1.size()/5);
            for(int i = 0;i<t1.size();i+=2){
                int xa = t1.get(i)[0];
                int ya = t1.get(i)[1];
                int xb = t1.get(i+1)[2];
                int yb = t1.get(i+1)[3];
                int[] t = new int[]{xa,ya,xb,yb};
                temp.add(t);
            }
            if(isRotated) rotatedPoints = temp;
            else points = temp;
            k--;
        }
        currentIteration = slider.getValue();
    }

    private void calculateMinkowski(int count){
        int xa;
        int ya;
        int xb;
        int yb;
        int xc;
        int yc;
        int xd;
        int yd;
        int xe;
        int ye;
        int xf;
        int yf;
        int xg;
        int yg;
        int xh;
        int yh;
        int xi;
        int yi;
        int sin90 = -1;
        int cos90 = 0;
        for (int i = 0; i < count; i++) {
            Vector<int[]> temp = new Vector<>();
            for (int[] t : points){
                xa = t[0];
                ya = t[1];
                xi = t[2];
                yi = t[3];

                xb = (int)(xa + (xi-xa) * 0.25);
                yb = (int)(ya + (yi-ya) * 0.25);

                xe = (int)(xa + (xi-xa) * 0.5);
                ye = (int)(ya + (yi-ya) * 0.5);

                xh = (int)(xa + (xi-xa) * 0.75);
                yh = (int)(ya + (yi-ya) * 0.75);

                xc = xb + (xe - xb) * cos90 - sin90 * (ye - yb);
                yc = yb + (xe - xb) * sin90 + cos90 * (ye - yb);

                xd = xc + (xe - xb);
                yd = yc + (ye - yb);

                xf = xe + (xh - xe) * cos90 - (yh - ye);
                yf = ye + (xh - xe) + cos90 * (yh - ye);

                xg = xf + (xh - xe);
                yg = yf + (yh - ye);

                temp.add(new int[]{xa,ya,xb,yb});
                temp.add(new int[]{xb,yb,xc,yc});
                temp.add(new int[]{xc,yc,xd,yd});
                temp.add(new int[]{xd,yd,xe,ye});
                temp.add(new int[]{xe,ye,xf,yf});
                temp.add(new int[]{xf,yf,xg,yg});
                temp.add(new int[]{xg,yg,xh,yh});
                temp.add(new int[]{xh,yh,xi,yi});
            }
            points = temp;
            if(isRotated) rotatedPoints = temp;
        }
        currentIteration = slider.getValue();
        lastForm = 2;
    }
    private void decreaseMinkowski(int k){
        Vector<int[]> t1;
        while (k>0){
            if(isRotated) t1 = rotatedPoints;
            else t1 = points;
            Vector<int[]> temp = new Vector<>(t1.size());
            System.out.println(t1.size());
            for(int i = 0;i<t1.size();i+=8){
                int xa = t1.get(i)[0];
                int ya = t1.get(i)[1];
                int xc = t1.get(i+7)[2];
                int yc = t1.get(i+7)[3];
                int[] t = new int[]{xa,ya,xc,yc};
                temp.add(t);
            }
            if(isRotated) rotatedPoints = temp;
            else points = temp;
            k--;
        }
        currentIteration = slider.getValue();
    }

    private void calculateHilbert(int count){

    }


    private void calculateDragon(int count){
        for (int i = 0;i<count;i++){
            Vector<int[]> temp;
            if (isRotated) temp = new Vector<>(rotatedPoints);
            else temp = new Vector<>(points);
            int lastX = temp.get(temp.size()-1)[2];
            int lastY = temp.get(temp.size()-1)[3];
            Vector<int[]> rotatedTemp = rotatePointsAroundPoint(temp,lastX,lastY);
            Collections.reverse(rotatedTemp);
            temp.addAll(rotatedTemp);
            points = temp;
            if(isRotated) rotatedPoints = temp;
        }
        currentIteration = slider.getValue();
        lastForm = 4;
    }
    private Vector<int[]> rotatePointsAroundPoint(Vector<int[]> vec, int axisX, int axisY){
        int cos = 0;
        int sin = -1;
        Vector<int[]> temp = new Vector<>();
        for (int[] i : vec){
            int i0 = (int) (cos * (i[0] - axisX) + sin * (i[1] - axisY)) + axisX;
            int i1 = (int) (cos * (i[1] - axisY) - sin * (i[0] - axisX)) + axisY;
            int i2 = (int) (cos * (i[2] - axisX) + sin * (i[3] - axisY)) + axisX;
            int i3 = (int) (cos * (i[3] - axisY) - sin * (i[2] - axisX)) + axisY;
            temp.add(new int[]{i2,i3,i0,i1});
        }
        return temp;
    }
    private void decreaseDragon(int k){
        for (int i = 0;i<k;i++){
            Vector<int[]> temp;
            if (isRotated) temp = rotatedPoints;
            else temp = points;
            temp.setSize(temp.size()/2);
        }
        currentIteration = slider.getValue();
    }
    private void calculatePeano(int count){
        Vector<int[]> temp;
        if (isRotated) temp = new Vector<>(rotatedPoints);
        else temp = new Vector<>(points);
        int k = (int) Math.pow(3,count-1);
        int segment = drawPanel.getWidth()/(k*3);
        int mX = 100;
        int mY = 467;
        for (int m = 0;m<k;m++){
            for (int n = 0;n<k;n++){
                int kf = 0;
                if (m%2 == 0 & n%2 !=0) kf = 8;
                if (m%2 != 0 & n%2 != 1) kf = 8;
                temp.addAll(getPeanoSample(kf,segment, segment*m*3-mX,drawPanel.getWidth() - mY - segment*n*3));
                if(m%2 != 1 & n%2 != 1 & n<k-1){
                    temp.add(new int[]{temp.get(temp.size()-1)[2], temp.get(temp.size()-1)[3],temp.get(temp.size()-1)[2],
                    temp.get(temp.size()-1)[3] - segment});
                }
                if(m%2 != 0 & n%2 != 0 & n<k-1){
                    temp.add(new int[]{temp.get(temp.size()-1)[2], temp.get(temp.size()-1)[3],temp.get(temp.size()-1)[2],
                            temp.get(temp.size()-1)[3] - segment});
                }
                if(m%2 !=1 & n%2 == 1 & n<k-1){
                    temp.add(new int[]{temp.get(temp.size()-1)[2], temp.get(temp.size()-1)[3],temp.get(temp.size()-1)[2],
                            temp.get(temp.size()-1)[3] - segment});
                }
                if(n==k-1 & m%2!=1 & m!=k-1){
                    temp.add(new int[]{temp.get(temp.size()-1)[2], temp.get(temp.size()-1)[3],temp.get(temp.size()-1)[2] + segment,
                            temp.get(temp.size()-1)[3]});
                }
                if(n==0 &  m%2==1){
                    temp.add(new int[]{temp.get(temp.size()-5)[0], temp.get(temp.size()-5)[1],temp.get(temp.size()-5)[0] + segment,
                            temp.get(temp.size()-5)[1]});
                }
                if(m%2 == 1 & n!=0 & n!=k-1){
                    temp.add(new int[]{temp.get(temp.size()-6)[0], temp.get(temp.size()-6)[1],temp.get(temp.size()-6)[0],
                            temp.get(temp.size()-6)[1] + segment});
                }

            }
        }
        points = temp;
        if(isRotated) rotatedPoints = temp;
        currentIteration = slider.getValue();
        lastForm = 5;
        for (int[] i : points){
        }
    }
    private Vector<int[]> getPeanoSample(int k, int len, int startX, int startY){
        Vector<int[]> temp = new Vector<>();
        if (k==0){
            temp.add(new int[]{startX + len/2, startY - len/2, startX + len/2, startY - len/2 - len*2});
            temp.add(new int[]{startX + len/2, startY - len/2 - len*2, startX + len/2 + len, startY - len/2 - len*2});
            temp.add(new int[]{startX + len/2 + len, startY - len/2 - len*2,startX + len/2 + len, startY - len/2});
            temp.add(new int[]{startX + len/2 + len, startY - len/2, startX + len/2 + len*2, startY - len/2});
            temp.add(new int[]{startX + len/2 + len*2, startY - len/2, startX + len/2 + len*2, startY - len/2 - len*2});
        }
        if(k == 8 ){
            temp.add(new int[]{startX + len/2 + len*2, startY - len/2, startX + len/2 + len*2, startY - len/2 - len*2});
            temp.add(new int[]{startX + len/2 + len*2, startY - len/2 - len*2,startX + len/2 + len, startY - len/2 - len*2});
            temp.add(new int[]{startX + len/2 + len, startY - len/2 - len*2,startX + len/2 + len, startY - len/2});
            temp.add(new int[]{startX + len/2 + len, startY - len/2,startX + len/2, startY - len/2});
            temp.add(new int[]{startX + len/2, startY - len/2,startX + len/2, startY - len/2 - len*2});
        }
        return temp;
    }
    private void calculateSierpinsky(int count){
            //x1
        //x0    x2   for int[x0,y0,x1,y1,x2,y2]

        for (int i = 0;i<count;i++){
            Vector<int[]> newTemp = new Vector<>();
            Vector<int[]> temp;
            if (isRotated) temp = rotatedPoints;
            else temp = points;
            for (int[] i1 : temp){
                int x0 = i1[0];
                int y0 = i1[1];
                int x1 = i1[2];
                int y1 = i1[3];
                int x2 = i1[4];
                int y2 = i1[5];
                int x01 = (x0 + x1)/2;
                int y01 = (y0 + y1)/2;
                int x02 = (x0 + x2)/2;
                int y02 = (y0 + y2)/2;
                int x12 = (x1 + x2)/2;
                int y12 = (y1 + y2)/2;
                newTemp.add(new int[]{x0,y0,x01,y01,x02,y02});
                newTemp.add(new int[]{x01,y01,x1,y1,x12,y12});
                newTemp.add(new int[]{x02,y02,x12,y12,x2,y2});
            }
            points = newTemp;
            if(isRotated) rotatedPoints = newTemp;
        }
        currentIteration = slider.getValue();
        lastForm = 6;
    }
    private void calculateCantor(int count){

        //x0y0   x01y01       x11y11      x1y1
        //int[]{x0,y0,len}
        for (int i = 0;i<count;i++){
            Vector<int[]> newTemp = new Vector<>();
            Vector<int[]> temp;
            if (isRotated) temp = rotatedPoints;
            else temp = points;
            for (int[] i1 : temp){
                int x0 = i1[0];
                int y0 = i1[1];
                int len = i1[2];
                int x11 = x0 + len/3*2;
                newTemp.add(new int[]{x0,y0,len/3});
                newTemp.add(new int[]{x11, y0, len/3});
            }
            System.out.println(" ");
            points = newTemp;
            if(isRotated) rotatedPoints = newTemp;
        }
        currentIteration = slider.getValue();
        lastForm = 7;
    }
}
