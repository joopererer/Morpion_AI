package Controleur;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class MinmaxAI {

    static final int INFINITY = 100 ;
    static final int ALPHA = 0;
    static final int BETA  = 1;
    Random r;

    class Node {
        int value;
        //int[] pos;
        Object action;
        List<Node> fils;
        boolean isCut;

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

    int cut_count = 0;

    public Node getNext(int depth){
        cut_count = 0;
        Node node = new Node();
        alpha_beta(node, depth, -INFINITY, INFINITY, true);
        System.out.println("cut_count:"+cut_count);

        printTree(node);

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

    interface EnumerListener {
        public boolean try_action(Object action);
    }

    public int alpha_beta(Node node, int depth, int alpha, int beta, boolean maximizingPlayer) {
        if(depth==0 || isTerminer()){
            return evalue(maximizingPlayer?getPlayerID():getEnnemieID());
        }

        int jouer = 0;
        int[] alpha_beta = new int[]{alpha, beta};
        if(maximizingPlayer){
            jouer = getPlayerID();
            enumer_all_possibilities(jouer, new EnumerListener(){
                @Override
                public boolean try_action(Object action) {
                    Node fils = new Node();
                    node.addFils(fils);
                    fils.action = action;
                    int value = alpha_beta(fils, depth-1, alpha_beta[ALPHA], alpha_beta[BETA], false);
                    fils.value = value;
                    alpha_beta[ALPHA] = Math.max(alpha_beta[ALPHA], value);
                    if(alpha_beta[ALPHA] >= alpha_beta[BETA]){
                        cut_count += 1;
                        //System.out.println("Cut --> best:"+alpha_beta[ALPHA]+" > beta:"+alpha_beta[BETA]+" ... "+isTerminer());
                        node.value = alpha_beta[ALPHA];
                        return false;
                    }
                    return true;
                }
            });
            node.value = alpha_beta[ALPHA];
            //node.pos = bestPos;
            return alpha_beta[ALPHA];
        }else{
            jouer = getEnnemieID();
            enumer_all_possibilities(jouer, new EnumerListener(){
                @Override
                public boolean try_action(Object action) {
                    Node fils = new Node();
                    node.addFils(fils);
                    fils.action = action;
                    int value = alpha_beta(fils, depth-1, alpha_beta[ALPHA], alpha_beta[BETA], true);
                    fils.value = value;
                    alpha_beta[BETA] = Math.min(alpha_beta[BETA], value);
                    if(alpha_beta[ALPHA] >= alpha_beta[BETA]){
                        cut_count += 1;
                        //System.out.println("Cut --> best:"+alpha_beta[BETA]+" < alpha:"+alpha_beta[ALPHA]+" ... "+isTerminer());
                        node.value = alpha_beta[BETA];
                        return false;
                    }
                    return true;
                }
            });
            node.value = alpha_beta[BETA];
            //node.pos = bestPos;
            return alpha_beta[BETA];
        }
    }

    protected abstract int getPlayerID();

    protected abstract int getEnnemieID();

    protected abstract void enumer_all_possibilities(int player, EnumerListener enumerListener);

    protected abstract int evalue(int playerID);

    protected abstract boolean isTerminer();

    private void printTree(Node node) {
        System.out.println(node.value);

        List<Node> file1 = new ArrayList<>();
        List<Node> file2 = new ArrayList<>();

        // 1
        if(node.fils!=null){
            if(node.fils!=null) {
                for (Node fils : node.fils) {
                    if (fils.isCut) {
                        System.out.print(" |(" + fils.value + ")");
                    } else {
                        System.out.print(" |" + fils.value);
                    }
                    file1.add(fils);
                }
            }
        }
        System.out.println();

        // 2
        while(!file1.isEmpty()){
            node = file1.remove(0);
            if(node.fils!=null) {
                for (Node fils : node.fils) {
                    if (fils.isCut) {
                        System.out.print(" |(" + fils.value + ")");
                    } else {
                        System.out.print(" |" + fils.value);
                    }
                    file2.add(fils);
                }
                System.out.print(" || ");
            }
        }
        System.out.println();

        // 3
        String[] str = new String[9];
        while(!file2.isEmpty()){
            node = file2.remove(0);
            if(node.fils!=null){
                int count = 0;
                for(Node fils : node.fils){
                    if(str[count]==null){
                        str[count] = "";
                    }
                    String txt = null;
                    if(fils.isCut){
                        txt = "  |("+fils.value+")";
                    }else{
                        txt = "  |"+fils.value;
                    }
                    str[count] += txt;
                    count++;
                }
                //System.out.print(" || ");
            }
        }

        for(String txt : str){
            if(txt!=null){
                System.out.println(txt);
            }
        }
        System.out.println();

//		List<Node> file = new ArrayList<>();
//		file.add(node);
//
//
//		while(!file.isEmpty()){
//			node = file.remove(0);
//			System.out.print(" |"+node.value);
//			if(node.fils!=null){
//				for(Node fils : node.fils){
//					file.add(fils);
//				}
//			}
//		}
    }

}
