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
import java.util.Stack;

import Modele.Jeu;

import javax.swing.*;

class JoueurIA extends Joueur {
	Random r;

	JoueurIA(int n, Jeu p) {
		super(n, p);
		r = new Random();
	}

	int cut_count = 0;

	@Override
	boolean tempsEcoule() {
		cut_count = 0;
		Node node = new Node();
		alpha_beta(plateau, node, 3, -INFINITY, INFINITY, true);
		//negamax(plateau, node, 3, -INFINITY, INFINITY, true);
		System.out.println("cut_count:" + cut_count);

		printTree(node);

		List<Node> pos_possible = new ArrayList<>();
		int value = node.value;
		for (Node fils : node.fils) {
			if (fils.value == value) {
				pos_possible.add(fils);
			}
		}
		System.out.println("value:" + node.value + " size:" + pos_possible.size());
		for (Node pos : pos_possible) {
			System.out.println("-> (" + pos.pos[0] + "," + pos.pos[1] + ")");
		}
		if (pos_possible.size() <= 0) {
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

	private void printTree(Node node) {
		System.out.println(node.value);

		List<Node> file1 = new ArrayList<>();
		List<Node> file2 = new ArrayList<>();

		// 1
		if (node.fils != null) {
			if (node.fils != null) {
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
		while (!file1.isEmpty()) {
			node = file1.remove(0);
			if (node.fils != null) {
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
		while (!file2.isEmpty()) {
			node = file2.remove(0);
			if (node.fils != null) {
				int count = 0;
				for (Node fils : node.fils) {
					if (str[count] == null) {
						str[count] = "";
					}
					String txt = null;
					if (fils.isCut) {
						txt = "  |(" + fils.value + ")";
					} else {
						txt = "  |" + fils.value;
					}
					str[count] += txt;
					count++;
				}
				//System.out.print(" || ");
			}
		}

		for (String txt : str) {
			if (txt != null) {
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

	class Node {
		int value;
		int[] pos;
		List<Node> fils;
		boolean isCut;

		public void addFils(Node node) {
			if (fils == null) {
				fils = new ArrayList<>();
			}
			fils.add(node);
		}
	}

	static final int INFINITY = 100;   // 表示无穷的值
	static final int WIN = +INFINITY;   // MAX的最大利益为正无穷
	static final int LOSE = -INFINITY;   // MAX的最小得益（即MIN的最大得益）为负无穷
	static final int DOUBLE_LINK = INFINITY / 2;   // 如果同一行、列或对角上连续有两个，赛点
	static final int INPROGRESS = 1;   // 仍可继续下（没有胜出或和局）
	static final int DRAW = 0;   // 和局

	public int alpha_beta(Jeu jeu, Node node, int depth, int alpha, int beta, boolean maximizingPlayer) {
		if (depth == 0 || jeu.isTerminer()) {
			//node.value = evalue(jeu, 1);
			int value = evaluate(jeu, maximizingPlayer?1:0);//evalue(jeu, 1);
			return value;//maximizingPlayer ? value : -value;
		}

		int jouer = 0;
		if (maximizingPlayer) {
			jouer = 1;
			for (int i = 0; i < jeu.largeur(); i++) {
				for (int j = 0; j < jeu.hauteur(); j++) {
					if (jeu.libre(i, j)) {
						Node fils = new Node();
						node.addFils(fils);
						fils.pos = new int[]{i, j};
						// position possible
						jeu.move(i, j, jouer);
						int value = alpha_beta(jeu, fils, depth - 1, alpha, beta, false);
						jeu.unmove(i, j);
						fils.value = value;
						alpha = Math.max(alpha, value);
						if(alpha >= beta){
							cut_count += 1;
							fils.isCut = true;
							node.value = alpha;
							return alpha;
						}
					}
				}
			}
			node.value = alpha;
			//node.pos = bestPos;
			return alpha;
		} else {
			jouer = 0;
			for (int i = 0; i < jeu.largeur(); i++) {
				for (int j = 0; j < jeu.hauteur(); j++) {
					if (jeu.libre(i, j)) {
						Node fils = new Node();
						node.addFils(fils);
						fils.pos = new int[]{i, j};
						// position possible
						jeu.move(i, j, jouer);
						int value = alpha_beta(jeu, fils, depth - 1, alpha, beta, true);
						jeu.unmove(i, j);
						fils.value = value;
						beta = Math.min(beta, value);
						if(alpha >= beta){
							cut_count += 1;
							fils.isCut = true;
							node.value = beta;
							return beta;
						}
					}
				}
			}
			node.value = beta;
			//node.pos = bestPos;
			return beta;
		}
	}

	public int negamax(Jeu jeu, Node node, int depth, int alpha, int beta, boolean isComputer) {
		if (depth == 0 || jeu.isTerminer()) {
			return evalue(jeu, isComputer ? 1 : 0);
		}

		int jouer = isComputer ? 1 : 0;
		for (int i = 0; i < jeu.largeur(); i++) {
			for (int j = 0; j < jeu.hauteur(); j++) {
				if (jeu.libre(i, j)) {
					Node fils = new Node();
					node.addFils(fils);
					fils.pos = new int[]{i, j};
					// position possible
					jeu.move(i, j, jouer);
					int value = -negamax(jeu, fils, depth - 1, -beta, -alpha, !isComputer);
					jeu.unmove(i, j);
					fils.value = value;
					alpha = Math.max(alpha, value);
					if (alpha >= beta) {
						cut_count += 1;
						fils.isCut = true;
						node.value = alpha;
						return alpha;
					}
				}
			}
		}
		node.value = alpha;
		//node.pos = bestPos;
		return alpha;
	}

	private int evalue(Jeu jeu, int playerAI) {
		if (jeu.isTerminer()) {
			if (jeu.isWin()) {
				if (jeu.getWinner() == playerAI) {
					return WIN;
				} else {
					return LOSE;
				}
			}
			return DRAW;
		} else {
			int find1 = 0;
			int find2 = 0;
			int count = 0;
			boolean hasEmpty = false;
			int chess = -1;
			for (int i = 0; i < jeu.hauteur(); i++) {
				count = 0;
				chess = -1;
				hasEmpty = false;
				for (int j = 0; j < jeu.largeur(); j++) {
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

			for (int j = 0; j < jeu.largeur(); j++) {
				count = 0;
				chess = -1;
				hasEmpty = false;
				for (int i = 0; i < jeu.hauteur(); i++) {
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
			for (int i = 0; i < jeu.hauteur(); i++) {
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
			for (int i = 0; i < jeu.hauteur(); i++) {
				int value = jeu.valeur(i, jeu.hauteur() - 1 - i);
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

			if (find1 > 0) {
				return DOUBLE_LINK;
			}
			if (find2 > 0) {
				return -DOUBLE_LINK;
			}
		}
		return INPROGRESS;
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
}