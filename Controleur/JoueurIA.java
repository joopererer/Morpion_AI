package Controleur;
/*
 * Morpion pédagogique

 * Copyright (C) 2016 Guillaume Huard

 * Ce programme est libre, vous pouvez le redistribuer et/ou le
 * modifier selon les termes de la Licence Publique Générale GNU publiée par la
 * Free Software Foundation (version 2 ou bien toute autre version ultérieure
 * choisie par vous).

 * Ce programme est distribué car potentiellement utile, mais SANS
 * AUCUNE GARANTIE, ni explicite ni implicite, y compris les garanties de
 * commercialisation ou d'adaptation dans un but spécifique. Reportez-vous à la
 * Licence Publique Générale GNU pour plus de détails.

 * Vous devez avoir reçu une copie de la Licence Publique Générale
 * GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307,
 * États-Unis.

 * Contact: Guillaume.Huard@imag.fr
 *          Laboratoire LIG
 *          700 avenue centrale
 *          Domaine universitaire
 *          38401 Saint Martin d'Hères
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import Modele.Jeu;

class JoueurIA extends Joueur {
	Random r;

	JoueurIA(int n, Jeu p) {
		super(n, p);
		r = new Random();
	}

	@Override
	boolean tempsEcoule() {
		Node node = new Node();
		alpha_beta(plateau, node, 6, -INFINITY, INFINITY, plateau.currentPlayer()==1);

		List<Node> pos_possible = new ArrayList<>();
		int value = node.value;
		for(Node fils : node.fils){
			if(fils.value == value){
				pos_possible.add(fils);
			}
		}
		System.out.println("value:"+node.value+" size:"+pos_possible.size());
		for(Node pos : pos_possible){
			System.out.println("-> ("+pos.pos[0]+","+pos.pos[1]+")");
		}
		if(pos_possible.size()<=0){
			// Pour cette IA, on selectionne aléatoirement une case libre
			int i, j;

			i = r.nextInt(plateau.hauteur());
			j = r.nextInt(plateau.largeur());
			while (!plateau.libre(i, j)) {
				i = r.nextInt(plateau.hauteur());
				j = r.nextInt(plateau.largeur());
			}
			plateau.jouer(i, j);
		} else {
			int index = r.nextInt(pos_possible.size());
			node = pos_possible.get(index);
			plateau.jouer(node.pos[0], node.pos[1]);
		}
		return true;
	}

	class Node {
		int value;
		int[] pos;
		List<Node> fils;

		public void addFils(Node node){
			if(fils==null){
				fils = new ArrayList<>();
			}
			fils.add(node);
		}
	}

	static final int   INFINITY = 100 ;   // 表示无穷的值
	static final int   WIN = +INFINITY ;   // MAX的最大利益为正无穷
	static final int   LOSE = -INFINITY ;   // MAX的最小得益（即MIN的最大得益）为负无穷
	static final int   DOUBLE_LINK = INFINITY / 2 ;   // 如果同一行、列或对角上连续有两个，赛点
	static final int   INPROGRESS = 1 ;   // 仍可继续下（没有胜出或和局）
	static final int   DRAW = 0 ;   // 和局

	public void alpha_beta(Jeu jeu, Node node, int depth, int alpha, int beta, boolean maximizingPlayer) {
		//Result result = null;
		if(depth==0 || jeu.isTerminer()){
			node.value = evalue(jeu, 1);
			//return result;
			return;
		}

//		int sum = 0;
//		int[] pos = new int[2];
//		for(int i=0; i<jeu.hauteur(); i++) {
//			for (int j = 0; j < jeu.largeur(); j++) {
//				int value = jeu.valeur(i, j);
//				if(value!=-1){
//					sum = value;
//					pos[0] = i;
//					pos[1] = j;
//					break;
//				}
//			}
//		}
//		// 如果是初始状态，则使用开局库
//		boolean isInitial = (jeu.getLibre() == 8) && (sum==1 ||sum==0 );
//		if (isInitial){
//			node.value = (sum==1 ? 1:-1)*INITIAL_POS_VALUE[pos[0]][pos[1]];
//		}

		int jouer = 0;
		if(maximizingPlayer){
			jouer = 1;
			int bestValue = -INFINITY;
			//int[] bestPos = null;
			for(int i=0; i<jeu.largeur(); i++){
				for(int j=0; j<jeu.hauteur(); j++){
					if(jeu.libre(i, j)){
						Node fils = new Node();
						node.addFils(fils);
						fils.pos = new int[]{i, j};
						// position possible
						jeu.move(i, j, jouer);
						alpha_beta(jeu, fils, depth-1, alpha, beta, false);
						jeu.unmove(i, j);
						//fils.value = result.value;
						if(fils.value > bestValue){
							bestValue = fils.value;
							//bestPos = fils.pos_ld;
						}
						if(bestValue>beta){
							break;
						}
						//bestValue = Math.max(bestValue, fils.value);
						alpha = Math.max(alpha, bestValue);
					}
				}
			}
			node.value = bestValue;
			//node.pos = bestPos;
		}else{
			jouer = 0;
			int bestValue = INFINITY;
			//int[] bestPos = null;
			for(int i=0; i<jeu.largeur(); i++){
				for(int j=0; j<jeu.hauteur(); j++){
					if(jeu.libre(i, j)){
						Node fils = new Node();
						node.addFils(fils);
						fils.pos = new int[]{i, j};
						// position possible
						jeu.move(i, j, jouer);
						alpha_beta(jeu, fils, depth-1, alpha, beta, true);
						jeu.unmove(i, j);
						//fils.value = result.value;
						if(fils.value < bestValue){
							bestValue = fils.value;
							//bestPos = fils.pos_ld;
						}
						if(bestValue<alpha){
							break;
						}
						//bestValue = Math.min(bestValue, fils.value);
						beta = Math.min(beta, bestValue);
					}
				}
			}
			node.value = bestValue;
			//node.pos = bestPos;
		}
	}

	private int evalue(Jeu jeu, int playerAI) {
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

}