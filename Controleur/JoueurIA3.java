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
        protected int getPlayerID() {
            return player;
        }

        @Override
        protected int getEnnemieID() {
            return 1-player;
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
                            return;
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
                    if(plateau.valeur(i, j)==getPlayerID()){
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
        protected int evalue(int playerID) {
            return evaluate(plateau, playerID);
        }

        public int evaluate(Jeu jeu, int player) {
            int[][] board = jeu.plateau;
            // Check for a win or a loss
            if (jeu.isWin()) {
                return jeu.getWinner()==player ? INFINITY : -INFINITY;
            }

            int score = 0;
            // Count number of rows and columns with two of the same player's symbols
            for (int i = 0; i < board.length; i++) {
                int rowSum = 0, colSum = 0;
                for (int j = 0; j < board[0].length; j++) {
                    if (board[i][j] == player) {
                        rowSum++;
                    } else if (board[i][j] == opponent(player)) {
                        rowSum--;
                    }
                    if (board[j][i] == player) {
                        colSum++;
                    } else if (board[j][i] == opponent(player)) {
                        colSum--;
                    }
                }
                if (rowSum == 2) {
                    score += 10;
                }
                if (colSum == 2) {
                    score += 10;
                }
            }
            // Count number of diagonals with two of the same player's symbols
            int diagonal1Sum = 0, diagonal2Sum = 0;
            for (int i = 0; i < board.length; i++) {
                if (board[i][i] == player) {
                    diagonal1Sum++;
                } else if (board[i][i] == opponent(player)) {
                    diagonal1Sum--;
                }
                if (board[i][board.length - i - 1] == player) {
                    diagonal2Sum++;
                } else if (board[i][board.length - i - 1] == opponent(player)) {
                    diagonal2Sum--;
                }
            }
            if (diagonal1Sum == 2) {
                score += 10;
            }
            if (diagonal2Sum == 2) {
                score += 10;
            }
            return score;
        }

        private int opponent(int player) {
            return 1-player;
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
