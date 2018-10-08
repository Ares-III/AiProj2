/*
 * University of Central Florida
 * CAP4630 - Fall 2018
 * Minimax Algorithm Implementation
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
import pacsim.GhostCell;
import pacsim.PacmanCell;
import pacsim.PacMode;
import pacsim.PathCell;
import java.lang.Math;

class treeNode {

	int reward;
	Point currPos;
	int lvl;
	ArrayList<treeNode> children;

	public treeNode(int value, Point position, int level)
	{
		reward = value;
		currPos = position;
		lvl = level;
		children = new ArrayList<>();
	}
}

class tree {

	PacCell[][] grid;
	int depth;
	treeNode root;

	treeNode maxPos;
	int maxPointVal;

	// int score;
	// Point next;
	// int max = -9999;
	// Point n;
	// Point e;
	// Point s;
	// Point w;

	final int[] x = {-1,0,1,0};
	final int[] y = {0,1,0,-1};

	public tree(Object state, int depth, Point position)
	{
		this.depth = depth;
		grid = (PacCell[][]) state;
		root = new treeNode(0, position, 0);
		maxPos = null;
		maxPointVal = Integer.MIN_VALUE;
		growTree(root);
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
	private int eval(Point cur, Point newPoint) {

		int score = 0;

		GhostCell nearestGhost = PacUtils.nearestGhost(cur, grid);
		Point nearestGoody = PacUtils.nearestGoody(cur, grid);

		int nearestGoodyFromCur = PacUtils.manhattanDistance(nearestGoody, cur);
		int nearestGhostFromCur = PacUtils.manhattanDistance(nearestGhost.getLoc(), cur);

		int nearestGoodyFromPoint = PacUtils.manhattanDistance(nearestGoody, newPoint);
		int nearestGhostFromPoint = PacUtils.manhattanDistance(nearestGhost.getLoc(), newPoint);

		if (nearestGhost.getMode().equals(PacMode.valueOf("CHASE"))) {

			if (nearestGhostFromPoint > nearestGhostFromCur)
				score += 5 * nearestGhostFromPoint;
			else if (nearestGhostFromPoint < nearestGhostFromCur)
				score -= 5 * nearestGhostFromPoint;
			if (nearestGoodyFromPoint > nearestGoodyFromCur)
				score -= 1 * nearestGoodyFromPoint;
			else if (nearestGoodyFromPoint < nearestGoodyFromCur)
				score += 1 * nearestGoodyFromPoint;
		}

		else if (nearestGhost.getMode().equals(PacMode.valueOf("SCATTER"))) {

			if (nearestGhostFromPoint > nearestGhostFromCur)
				score += 1 * nearestGhostFromPoint;
			else if (nearestGhostFromPoint < nearestGhostFromCur)
				score -= 1 * nearestGhostFromPoint;
			if (nearestGoodyFromPoint > nearestGoodyFromCur)
				score -= 5 * nearestGoodyFromPoint;
			else if (nearestGoodyFromPoint < nearestGoodyFromCur)
				score += 5 * nearestGoodyFromPoint;
		}

		else if (nearestGhost.getMode().equals(PacMode.valueOf("FEAR"))) {

			if (nearestGoodyFromPoint > nearestGoodyFromCur)
				score -= 3 * nearestGoodyFromPoint;
			else if (nearestGoodyFromPoint < nearestGoodyFromCur)
				score += 3 * nearestGoodyFromPoint;
		}

		return score;

	}

	// private Point findNext(Point cur, PacCell[][] grid) {
	//
	// 	PacCell move = PacUtils.neighbor(PacFace.valueOf("N"), cur, grid);
	// 	if (move instanceof PathCell) {
	// 		n = move.getLoc();
	// 		score = eval(cur, n);
	// 	}
	//
	// 	if (score > max) {
	// 		max = score;
	// 		next = n;
	// 	}
	//
	// 	move = PacUtils.neighbor(PacFace.valueOf("E"), cur, grid);
	// 	if (move instanceof PathCell) {
	// 		e = move.getLoc();
	// 		score = eval(cur, e);
	// 	}
	//
	// 	if (score > max) {
	// 		max = score;
	// 		next = e;
	// 	}
	//
	// 	move = PacUtils.neighbor(PacFace.valueOf("S"), cur, grid);
	// 	if (move instanceof PathCell) {
	// 		s = move.getLoc();
	// 		score = eval(cur, s);
	// 	}
	//
	// 	if (score > max) {
	// 		max = score;
	// 		next = s;
	// 	}
	//
	// 	move = PacUtils.neighbor(PacFace.valueOf("W"), cur, grid);
	// 	if (move instanceof PathCell) {
	// 		w = move.getLoc();
	// 		score = eval(cur, w);
	// 	}
	//
	// 	if (score > max) {
	// 		max = score;
	// 		next = w;
	// 	}
	//
	// 	return next;
	// }

	private void growTree(treeNode current)
	{
		String[] locations = {"N", "S", "E", "W"};

		for(int i = 0; i < 4; i++)
		{
			if(PacUtils.unoccupied(current.currPos.x+x[i], current.currPos.y+y[i], grid))
			{
				Point thePoint = new Point(current.currPos.x+x[i], current.currPos.y+y[i]);
				int pointVal = eval(current.currPos, thePoint);
				treeNode newNode = new treeNode(pointVal, thePoint, current.lvl+1);
				current.children.add(newNode);

				if(current.lvl+1 == depth && pointVal > maxPointVal)
				{
					maxPos = newNode;
					maxPointVal = pointVal;
				}
			}
		}
		if(current.lvl+1 >= depth)
		{
			return;
		}
		for(int i = 0; i < current.children.size(); i++)
		{
			growTree(current.children.get(i));
		}
	}

	public Point getNext()
	{
		if(maxPos == null)
		{
			return PacUtils.nearestUnoccupied(root.currPos, grid);
		}
		return maxPos.currPos;
	}

}

public class PacSimMinimax implements PacAction {

	int depth;
	private List<Point> path;

	public PacSimMinimax(int depth, String fname, int te, int gran, int max)
	{
		/* initialize variables */
		PacSim sim = new PacSim(fname, te, gran, max);
		this.depth = depth;
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
	public void init()
	{
		path = new ArrayList();
	}

	@Override
	public PacFace action(Object state)
	{
		PacCell[][] grid = (PacCell[][]) state;
		PacFace newFace = null;
		PacmanCell pc = PacUtils.findPacman( grid );

		if( pc == null ) return null;

		Point cur = pc.getLoc();

		tree makeDecision = new tree(state, depth, cur);

		Point mimax = makeDecision.getNext();

		path = BFSPath.getPath(grid, pc.getLoc(), mimax);
		Point next = path.remove( 0 );

		newFace = PacUtils.direction(cur, next);
	   grid = PacUtils.movePacman(cur, next, grid);

		return newFace;
	}
}
