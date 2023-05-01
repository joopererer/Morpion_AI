package Controleur;

import Modele.Jeu;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JoueurIA3 extends Joueur {

    Random r;
    int player;

    MinmaxAI ai = new MinmaxAI() {

        static final int   WIN = +INFINITY ;   // MAX的最大利益为正无穷
        static final int   LOSE = -INFINITY ;   // MAX的最小得益（即MIN的最大得益）为负无穷
        static final int   DOUBLE_LINK = INFINITY / 2 ;   // 如果同一行、列或对角上连续有两个，赛点
        static final int   INPROGRESS = 1 ;   // 仍可继续下（没有胜出或和局）
        static final int   DRAW = 0 ;   // 和局

        @Override
        protected int getPlayerHuman() {
            return 1-player;
        }

        @Override
        protected int getPlayerAI() {
            return player;
        }

        @Override
        protected void enumer_all_possibilities(int player, EnumerListener enumerListener) {
            for(int i=0; i<plateau.largeur(); i++){
                for(int j=0; j<plateau.hauteur(); j++){
                    if(plateau.libre(i, j)){
                        // position possible
                        plateau.move(i, j, player);
                        if(!enumerListener.try_action(new int[]{i, j})){
                            plateau.unmove(i, j);
                            break;
                        }
                        plateau.unmove(i, j);
                    }
                }
            }
        }

        static final int[][] INITIAL_POS_VALUE = {
                {3, 2, 3},
                {2, 4, 2},
                {3, 2, 3}
        };

        @Override
        public Node getNext(int depth){
            int count = 0;
            int[] pos = new int[2];
            int value = 0;
            for(int i=0; i<plateau.hauteur(); i++) {
                for (int j = 0; j < plateau.largeur(); j++) {
                    if(plateau.libre(i, j) && INITIAL_POS_VALUE[i][j]>value){
                        value = INITIAL_POS_VALUE[i][j];
                        pos[0] = i;
                        pos[1] = j;
                    }
                    if(plateau.valeur(i, j)==getPlayerAI()){
                        count += 1;
                        break;
                    }
                }
            }
            // 如果是初始状态，则使用开局库
            boolean isInitial = (count==0);
            if (isInitial){
                Node node = new Node();
                node.value = value;
                node.action = pos;
                return node;
            }
            return super.getNext(depth);
        }

        @Override
        protected int currentPlayer() {
            return plateau.currentPlayer();
        }

        @Override
        protected int evalue(int playerAI) {
            Jeu jeu = plateau;
            if(jeu.isTerminer()){
                if(jeu.isWin()){
                    if(jeu.getWinner()==playerAI){
                        return WIN;
                    }else{
                        return LOSE;
                    }
                }
                return DRAW;
            }else{
                int find1 = 0;
                int find2 = 0;
                int count = 0;
                boolean hasEmpty = false;
                int chess = -1;
                for(int i=0; i<jeu.hauteur(); i++){
                    count = 0;
                    chess = -1;
                    hasEmpty = false;
                    for(int j=0; j<jeu.largeur(); j++){
                        int value = jeu.valeur(i, j);
                        if(value==-1){
                            hasEmpty = true;
                        }else{
                            if(chess==-1){
                                chess = value;
                            }
                            if(chess==value){
                                count++;
                            }
                        }
                        if(hasEmpty && count==jeu.largeur()-1){
                            if(chess==playerAI){
                                find1 += 1;
                            }else{
                                find2 += 1;
                            }
                        }
                    }
                }

                count = 0;
                chess = -1;
                hasEmpty = false;
                for(int j=0; j<jeu.largeur(); j++){
                    count = 0;
                    chess = -1;
                    hasEmpty = false;
                    for(int i=0; i<jeu.hauteur(); i++) {
                        int value = jeu.valeur(i, j);
                        if (value == -1) {
                            hasEmpty = true;
                        } else {
                            if (chess == -1) {
                                chess = value;
                            }
                            if (chess == value) {
                                count++;
                            }
                        }
                        if (hasEmpty && count == jeu.largeur() - 1) {
                            if (chess == playerAI) {
                                find1 += 1;
                            } else {
                                find2 += 1;
                            }
                        }
                    }
                }

                count = 0;
                chess = -1;
                hasEmpty = false;
                for(int i=0; i<jeu.hauteur(); i++){
                    int value = jeu.valeur(i, i);
                    if (value == -1) {
                        hasEmpty = true;
                    } else {
                        if (chess == -1) {
                            chess = value;
                        }
                        if (chess == value) {
                            count++;
                        }
                    }
                    if (hasEmpty && count == jeu.largeur() - 1) {
                        if (chess == playerAI) {
                            find1 += 1;
                        } else {
                            find2 += 1;
                        }
                    }
                }

                count = 0;
                chess = -1;
                hasEmpty = false;
                for(int i=0; i<jeu.hauteur(); i++){
                    int value = jeu.valeur(i, jeu.hauteur()-1-i);
                    if (value == -1) {
                        hasEmpty = true;
                    } else {
                        if (chess == -1) {
                            chess = value;
                        }
                        if (chess == value) {
                            count++;
                        }
                    }
                    if (hasEmpty && count == jeu.largeur() - 1) {
                        if (chess == playerAI) {
                            find1 += 1;
                        } else {
                            find2 += 1;
                        }
                    }
                }

                if(find2>0){
                    return -DOUBLE_LINK;
                }
                if(find1>0){
                    return DOUBLE_LINK;
                }
            }
            return INPROGRESS;
        }

        @Override
        protected boolean isTerminer() {
            return plateau.isTerminer();
        }
    };

    public JoueurIA3(int n, Jeu p, int player) {
        super(n, p);
        this.player = player;
        r = new Random(System.currentTimeMillis());
    }

    @Override
    boolean tempsEcoule() {
        MinmaxAI.Node node = ai.getNext(3);
        if(node!=null && node.action!=null){
            int[] pos = (int[])node.action;
            plateau.jouer(pos[0], pos[1]);
        }else{
            // Pour cette IA, on selectionne aléatoirement une case libre
            int i, j;

            i = r.nextInt(plateau.hauteur());
            j = r.nextInt(plateau.largeur());
            while (!plateau.libre(i, j)) {
                i = r.nextInt(plateau.hauteur());
                j = r.nextInt(plateau.largeur());
            }
            plateau.jouer(i, j);
        }
        return true;
    }


}
