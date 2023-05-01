package Controleur;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class MinmaxAI {

    static final int INFINITY = 100 ;
    Random r;

    class Node {
        int value;
        //int[] pos;
        Object action;
        List<Node> fils;

        public void addFils(Node node){
            if(fils==null){
                fils = new ArrayList<>();
            }
            fils.add(node);
        }
    }

    public MinmaxAI(){
        r = new Random(System.currentTimeMillis());
    }

    public Node getNext(int depth){
        Node node = new Node();
        alpha_beta(node, depth, new int[]{-INFINITY, INFINITY}, currentPlayer()==1);

        List<Node> pos_possible = new ArrayList<>();
        int value = node.value;
        for(Node fils : node.fils){
            if(fils.value == value){
                pos_possible.add(fils);
            }
        }
        System.out.println("value:"+node.value+" size:"+pos_possible.size());
//        for(Node pos : pos_possible){
//            System.out.println("-> ("+pos.pos[0]+","+pos.pos[1]+")");
//        }
        if(pos_possible.size()>0){
            int index = r.nextInt(pos_possible.size());
            node = pos_possible.get(index);
        }
        return node;
    }

    protected abstract int currentPlayer();

    interface EnumerListener {
        public boolean try_action(Object action);
    }

    public void alpha_beta(Node node, int depth, final int[] alpha_beta, boolean maximizingPlayer) {
        //Result result = null;
        if(depth==0 || isTerminer()){
            node.value = evalue(1);
            //return result;
            return;
        }

        int jouer = 0;
        if(maximizingPlayer){
            jouer = 1;//getPlayerAI();
            final int[] bestValue = {-INFINITY};
            enumer_all_possibilities(jouer, new EnumerListener(){
                @Override
                public boolean try_action(Object action) {
                    Node fils = new Node();
                    node.addFils(fils);
                    fils.action = action;
                    alpha_beta(fils, depth-1, alpha_beta, false);
                    if(fils.value > bestValue[0]){
                        bestValue[0] = fils.value;
                        //bestPos = fils.pos_ld;
                    }
                    if(bestValue[0] >alpha_beta[1]){
                        return false;
                    }
                    //bestValue = Math.max(bestValue, fils.value);
                    alpha_beta[0] = Math.max(alpha_beta[0], bestValue[0]);
                    return true;
                }
            });
            node.value = bestValue[0];
            //node.pos = bestPos;
        }else{
            jouer = 0;//getPlayerHuman();
            final int[] bestValue = {INFINITY};
            enumer_all_possibilities(jouer, new EnumerListener(){
                @Override
                public boolean try_action(Object action) {
                    Node fils = new Node();
                    node.addFils(fils);
                    fils.action = action;
                    alpha_beta(fils, depth-1, alpha_beta, true);
                    if(fils.value < bestValue[0]){
                        bestValue[0] = fils.value;
                        //bestPos = fils.pos_ld;
                    }
                    if(bestValue[0] < alpha_beta[0]){
                        return false;
                    }
                    //bestValue = Math.min(bestValue, fils.value);
                    alpha_beta[1] = Math.min(alpha_beta[1], bestValue[0]);
                    return true;
                }
            });
            node.value = bestValue[0];
            //node.pos = bestPos;
        }
    }

    protected abstract int getPlayerHuman();

    protected abstract int getPlayerAI();

    protected abstract void enumer_all_possibilities(int player, EnumerListener enumerListener);

    protected abstract int evalue(int playerAI);

    protected abstract boolean isTerminer();

}
