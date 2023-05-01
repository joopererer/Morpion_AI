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

import Modele.Jeu;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class JoueurIA2 extends Joueur {
	Random r;

	JoueurIA2(int n, Jeu p) {
		super(n, p);
		r = new Random();
	}

	@Override
	boolean tempsEcoule() {
		int[] pos = minmax(plateau, 3);
		if(pos==null){
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
			plateau.jouer(pos[0], pos[1]);
		}
		return true;
	}

	class Result {
		int value;
		int[] action;
	}

	class Node {
		int value;
		int[] pos;
		int[] pos_ld;
		List<Node> fils;

		public void addfils(Node node){
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

	public int[] minmax(Jeu jeu, int depth){
		int[][] bestMoves = new int[9][2];
		int index = 0;
		int bestValue = -INFINITY;
		for(int i=0; i<jeu.hauteur(); i++) {
			for (int j = 0; j < jeu.largeur(); j++) {
				if(jeu.libre(i, j)){
					jeu.move(i, j, 1);
					int value = min(jeu, depth, -INFINITY, INFINITY);
					if(value>bestValue){
						bestValue = value;
						index = 0;
						bestMoves[index][0] = i;
						bestMoves[index][1] = j;
					}else if(value==bestValue){
						index++;
						bestMoves[index][0] = i;
						bestMoves[index][1] = j;
					}
					jeu.unmove(i, j);
				}
			}
		}
		if(index>1){
			index = (r.nextInt()>>>1)%index;
		}
		return bestMoves[index];
	}

	private int max(Jeu jeu, int depth, int alpha, int beta){
		int evalValue = evalue(jeu, 1);
		if(beta <= alpha){
			return evalValue;
		}

		boolean isGameOver = (evalValue==WIN || evalValue==LOSE || evalValue==DRAW);
		if(depth==0 || isGameOver){
			return evalValue;
		}

		int player = 1;
		int bestValue = -INFINITY;
		for(int i=0; i<jeu.largeur(); i++){
			for(int j=0; j<jeu.hauteur(); j++){
				if(jeu.libre(i, j)){
					// position possible
					jeu.move(i, j, player);
					bestValue = Math.max(bestValue, min(jeu, depth-1, Math.max(bestValue, alpha), beta));
					jeu.unmove(i, j);
				}
			}
		}
		return evalValue;
		//return Math.max(evalValue, bestValue);
	}

	private int min(Jeu jeu, int depth, int alpha, int beta) {
		int evalValue = evalue(jeu, 1);
		if(beta <= alpha){
			return evalValue;
		}

		boolean isGameOver = (evalValue==WIN || evalValue==LOSE || evalValue==DRAW);
		if(depth==0 || isGameOver){
			return evalValue;
		}

		int player = 0;
		int bestValue = +INFINITY;
		for(int i=0; i<jeu.largeur(); i++){
			for(int j=0; j<jeu.hauteur(); j++){
				if(jeu.libre(i, j)){
					// position possible
					jeu.move(i, j, player);
					bestValue = Math.min(bestValue, max(jeu, depth-1, alpha, Math.min(bestValue, beta)));
					jeu.unmove(i, j);
				}
			}
		}
		return evalValue;
		//return Math.min(evalValue, bestValue);
	}
	static final int[][] INITIAL_POS_VALUE = {
			{3, 2, 3},
			{2, 4, 2},
			{3, 2, 3}
	};

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
			int count = 0;
			int chess = 0;
//			int[] pos = new int[2];
//			for(int i=0; i<jeu.hauteur(); i++) {
//				for (int j = 0; j < jeu.largeur(); j++) {
//					int value = jeu.valeur(i, j);
//					if(value!=-1){
//						count += 1;
//						chess = value;
//						pos[0] = i;
//						pos[1] = j;
//						break;
//					}
//				}
//			}
//			// 如果是初始状态，则使用开局库
//			boolean isInitial = (jeu.getLibre() == 8) && (count==1);
//			if (isInitial){
//				return (chess==1 ? 1:-1)*INITIAL_POS_VALUE[pos[0]][pos[1]];
//			}

			int find1 = 0;
			int find2 = 0;
			count = 0;
			boolean hasEmpty = false;
			chess = -1;
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