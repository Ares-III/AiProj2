/*
 * University of Central Florida
 * CAP4630 - Fall 2018
 * Authors: Arati Banerjee & Irene Tanner
 */

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import pacsim.BFSPath;
import pacsim.PacAction;
import pacsim.PacCell;
import pacsim.PacFace;
import pacsim.PacSim;
import pacsim.PacUtils;
import pacsim.PacmanCell;
import java.lang.Math;


public class PacSimMinimax implements PacAction {
	/* class and instance variables */

	public PacSimMinimax(int depth, String fname, int te, int gran, int max)
	{
		/* initialize variables */
		PacSim sim = new PacSim(fname, te, gran, max);
		sim.init(this);
	}

	public static void main(String[] args)
	{
		String fname = args[0];
		int depth = Integer.parseInt(args[1]);

		int te = 0;
		int gr = 0;
		int ml = 0;

		if(args.length == 5)
		{
			te = Integer.parseInt(args[2]);
			gr = Integer.parseInt(args[3]);
			ml = Integer.parseInt(args[4]);
		}

		new PacSimMinimax(depth, fname, te, gr, ml);
		System.out.println("\nAdversarial Search using Minimax by Arati Banerjee and Irene Tanner:");
		System.out.println("    Game board  : " + depth + "\n");
		System.out.println("    Search depth: " + depth + "\n");

		if(te>0)
		{
			System.out.println("    Preliminary runs : " + te
			+ "\n    Granularity      : " + gr
			+ "\n    Max move limit   : " + ml
			+ "\n\nPreliminary run results :\n");
		}
	}

	@Override
	public void init() {}

	@Override
	public PacFace action(Object state)
	{
		PacCell[][] grid = (PacCell[][]) state;
		PacFace newFace = null;
		PacmanCell pc = PacUtils.findPacman(grid);
		
		// max function finds highest score in array and returns point associated with that score
		// should also handle the case where scores are tied
		Point next = max(eval(pc.getLoc, grid));
		newFace = PacUtils.direction(pc.getLoc(), next);
	    grid = PacUtils.movePacman(pc.getLoc(), next, grid);
		
		
		return newFace;
	}
	
	/* 
	 * In our evaluation function, the score for each move is based off of
	 * ghost mode, distance from nearest ghost to pacman, and distance from
	 * nearest goody to pacman. In chase mode, pacman moving further from a 
	 * ghost is valued (scored) higher than moving closer to a goody. In scatter 
	 * mode, pacman moving closer to a goody is valued (scored) higher than moving 
	 * further from a ghost. In both modes, score is reduced if pacman moves 
	 * closer to a ghost or further from a goody. In fear mode, distance to ghost
	 * is disregarded, and score increases if pacman moves closer to a goody.
	 */
	public int[] eval(Point point, PacCell[][] cell) {
		
		Point nearestGoody = PacUtils.nearestGoody(point, cell);
		Point nearestGhost = PacUtils.nearestGhost(point, cell);
		
		int nearestGoodyFromCur = PacUtils.manhattanDistance(nearestGoody, point);
		int nearestGhostFromCur = PacUtils.manhattanDistance(nearestGhost, point);
		
		for each possible move newPoint {
			
			int nearestGoodyFromPoint = PacUtils.manhattanDistance(nearestGoody, newPoint);
			int nearestGhostFromPoint = PacUtils.manhattanDistance(nearestGhost, newPoint);
			
			if (GhostCell.getMode() == "chase") {
				
				if (nearestGhostFromPoint > nearestGhostFromCur)
					score += 2 * nearestGhostFromPoint;
				else if (nearestGhostFromPoint < nearestGhostFromCur)
					score -= 2 * nearestGhostFromPoint;
				if (nearestGoodyFromPoint > nearestGoodyFromCur)
					score -= 1 * nearestGoodyFromPoint;
				else if (nearestGoodyFromPoint < nearestGoodyFromCur)
					score += 1 * nearestGoodyFromPoint;
			}
			
			else if (GhostCell.getMode() == "scatter") {
				
				if (nearestGhostFromPoint > nearestGhostFromCur)
					score += 1 * nearestGhostFromPoint;
				else if (nearestGhostFromPoint < nearestGhostFromCur)
					score -= 1 nearestGhostFromPoint;
				if (nearestGoodyFromPoint > nearestGoodyFromCur)
					score -= 2 * nearestGoodyFromPoint;
				else if (nearestGoodyFromPoint < nearestGoodyFromCur)
					score += 2 * nearestGoodyFromPoint;
			}
			
			else if (GhostCell.getMode() == "fear") {
			
				if (nearestGoodyFromPoint > nearestGoodyFromCur)
					score -= 3 * nearestGoodyFromPoint;
				else if (nearestGoodyFromPoint < nearestGoodyFromCur)
					score += 3 * nearestGoodyFromPoint;
			}
		}
		
		return score;
		
	}
	

}
