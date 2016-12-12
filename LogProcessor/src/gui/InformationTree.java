package gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import commoninterface.entities.Entity;
import commoninterface.entities.GeoEntity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.RobotLocation;
import commoninterface.entities.VirtualEntity;
import commoninterface.entities.Waypoint;
import commoninterface.entities.formation.Formation;
import commoninterface.entities.formation.Target;
import commoninterface.utils.jcoord.LatLon;
import commoninterface.utils.logger.EntityManipulation;
import commoninterface.utils.logger.EntityManipulation.Operation;
import commoninterface.utils.logger.LogData;

public class InformationTree extends Container {
	private static final long serialVersionUID = 3703399377089846658L;
	private JTree jtree;
	private DefaultTreeModel jtreeModel;
	private DefaultMutableTreeNode robotsNode;
	private DefaultMutableTreeNode entitiesNode;

	private HashMap<String, RobotTreeNode> robotsNodes = new HashMap<String, RobotTreeNode>();
	private HashMap<String, EntityTreeNode> entitiesNodes = new HashMap<String, EntityTreeNode>();

	public InformationTree() {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Experiments subjects");
		robotsNode = new DefaultMutableTreeNode("Robots");
		entitiesNode = new DefaultMutableTreeNode("Entities");

		rootNode.add(robotsNode);
		rootNode.add(entitiesNode);

		jtreeModel = new DefaultTreeModel(rootNode);
		jtree = new JTree(jtreeModel);
		jtree.setShowsRootHandles(true);
		jtree.setRootVisible(false);
		jtree.setEditable(true);

		setLayout(new BorderLayout());
		JScrollPane jScroll = new JScrollPane(jtree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JViewport viewport = jScroll.getViewport();
		viewport.setLayout(new ConstrainedViewPortLayout());
		add(jScroll, BorderLayout.CENTER);

		setMinimumSize(jtree.getMinimumSize());
		setPreferredSize(jtree.getPreferredScrollableViewportSize());
	}

	/*
	 * Robots
	 */
	public void updateRobot(LogData logData) {
		if (robotsNodes.get(logData.ip) == null) {
			RobotTreeNode node = new RobotTreeNode(logData);
			robotsNodes.put(logData.ip, node);

			jtreeModel.insertNodeInto(node, robotsNode, robotsNode.getChildCount());
			jtree.expandPath(new TreePath(node.getPath()));
		} else {
			robotsNodes.get(logData.ip).updateNode(logData);
		}
	}

	public void updateRobot(EntityManipulation entityManipulation) {
		for (Entity entity : entityManipulation.getEntities()) {
			updateRobot(entity);
		}
	}

	public void updateRobot(Entity entity) {
		if (entity instanceof RobotLocation) {
			RobotLocation rl = (RobotLocation) entity;
			if (robotsNodes.get(rl.getName()) == null) {
				RobotTreeNode node = new RobotTreeNode(rl);
				robotsNodes.put(rl.getName(), node);

				jtreeModel.insertNodeInto(node, robotsNode, robotsNode.getChildCount());
				jtree.expandPath(new TreePath(node.getPath()));
			} else {
				robotsNodes.get(rl.getName()).updateNode(rl);
			}
		} else {
			throw new IllegalArgumentException("Illegal entity type (" + entity.getClass() + ")");
		}
	}

	public void removeRobot(String ip) {
		if (ip != null) {
			RobotTreeNode node = robotsNodes.remove(ip);

			if (node != null) {
				jtreeModel.removeNodeFromParent(node);
			}
		}
	}

	protected class RobotTreeNode extends DefaultMutableTreeNode {
		private static final long serialVersionUID = 6745991001576897626L;
		private Object sourceData;

		private DefaultMutableTreeNode type;
		private DefaultMutableTreeNode position;
		private DefaultMutableTreeNode orientation;
		private DefaultMutableTreeNode speed = null;
		private DefaultMutableTreeNode date = null;

		public RobotTreeNode(LogData logData) {
			super(logData.ip + " (T=" + logData.timestep + ")");
			this.sourceData = logData;

			type = new DefaultMutableTreeNode("Type: " + logData.droneType, false);
			position = new DefaultMutableTreeNode("Position: " + logData.latLon, false);
			orientation = new DefaultMutableTreeNode("Orientation: " + logData.GPSorientation, false);
			speed = new DefaultMutableTreeNode("Speed: " + logData.GPSspeed, false);
			date = new DefaultMutableTreeNode("Date: " + logData.GPSdate, false);

			add(type);
			add(position);
			add(orientation);
			add(speed);
			add(date);
		}

		public RobotTreeNode(RobotLocation rl) {
			super(rl.getName() + " (T=" + rl.getTimestepReceived() + ")");
			this.sourceData = rl;

			type = new DefaultMutableTreeNode("Type: " + rl.getDroneType(), false);
			position = new DefaultMutableTreeNode("Position: " + rl.getLatLon(), false);
			orientation = new DefaultMutableTreeNode("Orientation: " + rl.getOrientation(), false);

			add(type);
			add(position);
			add(orientation);
		}

		public void updateNode(LogData logData) {
			this.sourceData = logData;

			setUserObject(logData.ip + " (T=" + logData.timestep + ")");
			type.setUserObject("Type: " + logData.droneType);
			position.setUserObject("Position: " + logData.latLon);
			orientation.setUserObject("Orientation: " + logData.GPSorientation);

			if (speed != null) {
				speed.setUserObject("Speed: " + logData.GPSspeed);
			} else {
				speed = new DefaultMutableTreeNode("Speed: " + logData.GPSspeed, false);
				add(speed);
			}

			if (date != null) {
				date.setUserObject("Date: " + logData.GPSdate);
			} else {
				date = new DefaultMutableTreeNode("Date: " + logData.GPSdate, false);
				add(date);
			}

			update();
		}

		public void updateNode(RobotLocation rl) {
			this.sourceData = rl;

			setUserObject(rl.getName() + " (T=" + rl.getTimestepReceived() + ")");
			type.setUserObject("Type: " + rl.getDroneType());
			position.setUserObject("Position: " + rl.getLatLon());
			orientation.setUserObject("Orientation: " + rl.getOrientation());

			if (speed != null) {
				this.remove(speed);
				speed = null;
			}

			if (date != null) {
				this.remove(date);
				date = null;
			}

			update();
		}

		public Object getSourceData() {
			return sourceData;
		}

		private void update() {
			jtreeModel.nodeChanged(this);
			jtree.repaint();
		}
	}

	/*
	 * Entities
	 */
	public void updateEntity(EntityManipulation entityManipulation) {
		ArrayList<Entity> entities = entityManipulation.getEntities();
		switch (entityManipulation.getOperation()) {
		case ADD:
			for (Entity entity : entities) {
				if (entity instanceof RobotLocation) {
					updateRobot(entity);
				} else {
					EntityTreeNode entityTreeNode = new EntityTreeNode(entity);
					entitiesNodes.put(entity.getName(), entityTreeNode);
					jtreeModel.insertNodeInto(entityTreeNode, entitiesNode, entitiesNode.getChildCount());
					jtreeModel.nodeChanged(entityTreeNode);
					expandNodesAndChilds(entityTreeNode);
				}
			}
			break;
		case MOVE:
			for (Entity entity : entities) {
				if (entity instanceof RobotLocation) {
					updateRobot(entity);
				} else {
					EntityTreeNode toMoveEntity = entitiesNodes.get(entity.getName());

					if (toMoveEntity != null) {
						toMoveEntity.updateNode(entity);
						jtreeModel.nodeChanged(toMoveEntity);
					} else {
						EntityTreeNode entityTreeNode = new EntityTreeNode(entity);
						entitiesNodes.put(entity.getName(), entityTreeNode);
						jtreeModel.insertNodeInto(entityTreeNode, entitiesNode, entitiesNode.getChildCount());
						jtreeModel.nodeChanged(entityTreeNode);
						expandNodesAndChilds(entityTreeNode);
					}
				}
			}
			break;
		case REMOVE:
			for (Entity entity : entities) {
				if (entity instanceof RobotLocation) {
					removeRobot(entity.getName());
				} else {
					EntityTreeNode toRemoveNode = entitiesNodes.remove(entity.getName());

					if (toRemoveNode != null) {
						TreeNode parent = toRemoveNode.getParent();
						jtreeModel.removeNodeFromParent(toRemoveNode);
						jtreeModel.nodeChanged(parent);
					}
				}
			}
			break;
		}
	}

	protected class EntityTreeNode extends DefaultMutableTreeNode {
		private static final long serialVersionUID = 2225607210172584770L;
		private Entity entity;

		private DefaultMutableTreeNode type;
		private DefaultMutableTreeNode positionNode;
		private List<DefaultMutableTreeNode> positions;
		private DefaultMutableTreeNode formationType = null;
		private DefaultMutableTreeNode motionType = null;

		public EntityTreeNode(Entity entity) {
			super(entity.getName() + " (T=" + entity.getTimestepReceived() + ")");
			this.entity = entity;

			type = new DefaultMutableTreeNode("Type: " + entity.getClass().getSimpleName(), false);
			positions = new ArrayList<DefaultMutableTreeNode>();
			if (entity instanceof GeoFence) {
				LinkedList<Waypoint> waypoints = ((GeoFence) entity).getWaypoints();
				for (int i = 0; i < waypoints.size(); i++) {
					Waypoint wp = waypoints.get(i);
					positions.add(new DefaultMutableTreeNode("Waypoint \"" + wp.getName() + "\": " + wp.getLatLon()));
				}
			} else if (entity instanceof Formation) {
				Formation formation = (Formation) entity;

				positions.add(new DefaultMutableTreeNode("Center: " + formation.getLatLon()));
				List<Target> targets = formation.getTargets();

				if (targets != null && !targets.isEmpty()) {
					for (int i = 0; i < targets.size(); i++) {
						Target t = targets.get(i);
						positions.add(new DefaultMutableTreeNode("Target " + t.getName() + ": " + t.getLatLon()));
					}
				}

				formationType = new DefaultMutableTreeNode("Shape: " + formation.getFormationType());

				if (formation.getMotionData() != null) {
					motionType = new DefaultMutableTreeNode(
							"Motion type: " + formation.getMotionData().getClass().getSimpleName());
				}
			} else if (entity instanceof GeoEntity) {
				positions.add(new DefaultMutableTreeNode("Position: " + ((GeoEntity) entity).getLatLon(), false));
			} else if (entity instanceof VirtualEntity) {
				positions.add(new DefaultMutableTreeNode("Position: " + ((VirtualEntity) entity).getPosition(), false));
			}

			add(type);
			if (formationType != null) {
				add(formationType);
				jtreeModel.nodeChanged(formationType);
			}

			if (motionType != null) {
				add(motionType);
				jtreeModel.nodeChanged(motionType);
			}

			if (positions.size() == 1) {
				positionNode = positions.get(0);
			} else if (positions.size() > 1) {
				positionNode = new DefaultMutableTreeNode("Positions", true);
				for (DefaultMutableTreeNode node : positions) {
					positionNode.add(node);
				}
			}
			add(positionNode);
			jtreeModel.nodeChanged(positionNode);
		}

		public void updateNode(Entity entity) {
			this.entity = entity;

			setUserObject(entity.getName() + " (T=" + entity.getTimestepReceived() + ")");
			type.setUserObject("Type: " + entity.getClass().getSimpleName());

			if (entity instanceof Formation) {
				Formation formation = (Formation) entity;

				formationType.setUserObject("Shape: " + formation.getFormationType());
				if (formation.getMotionData() != null) {
					if (motionType != null) {
						motionType
								.setUserObject("Motion type: " + formation.getMotionData().getClass().getSimpleName());
					} else {
						motionType = new DefaultMutableTreeNode(
								"Motion type: " + formation.getMotionData().getClass().getSimpleName());
						add(formationType);
					}

					jtreeModel.nodeChanged(motionType);
				} else {
					if (motionType != null) {
						TreeNode parent = motionType.getParent();
						jtreeModel.removeNodeFromParent(motionType);
						motionType = null;

						jtreeModel.nodeChanged(parent);
					}
				}
			}

			if (entity instanceof GeoFence) {
				LinkedList<Waypoint> waypoints = ((GeoFence) entity).getWaypoints();
				for (int i = 0; i < waypoints.size(); i++) {
					Waypoint wp = waypoints.get(i);
					positions.get(i).setUserObject("Waypoint \"" + wp.getName() + "\": " + wp.getLatLon());
					jtreeModel.nodeChanged(positions.get(i));
				}
			} else if (entity instanceof Formation) {
				List<Target> targets = ((Formation) entity).getTargets();
				for (int i = 0; i < targets.size(); i++) {
					Target t = targets.get(i);
					positions.get(i).setUserObject("Target \"" + t.getName() + "\": " + t.getLatLon());
					jtreeModel.nodeChanged(positions.get(i));
				}
			} else if (entity instanceof GeoEntity) {
				positions.get(0).setUserObject("Position: " + ((GeoEntity) entity).getLatLon());
				jtreeModel.nodeChanged(positions.get(0));
			} else if (entity instanceof VirtualEntity) {
				positions.get(0).setUserObject("Position: " + ((VirtualEntity) entity).getPosition());
				jtreeModel.nodeChanged(positions.get(0));
			}

			jtreeModel.nodeChanged(positionNode);
			update();
		}

		public Entity getEntity() {
			return entity;
		}

		private void update() {
			jtreeModel.nodeChanged(this);
			jtree.repaint();
		}
	}

	/*
	 * Tools
	 */
	@SuppressWarnings("unused")
	private void expandAllNodes() {
		for (int i = 0; i < jtree.getRowCount(); i++) {
			jtree.expandRow(i);
		}
	}

	private void expandNodesAndChilds(TreeNode node) {
		jtree.expandPath(getPath(node));

		for (int i = 0; i < node.getChildCount(); i++) {
			expandNodesAndChilds(node.getChildAt(i));
		}
	}

	private static TreePath getPath(TreeNode treeNode) {
		List<Object> nodes = new ArrayList<Object>();
		if (treeNode != null) {
			nodes.add(treeNode);
			treeNode = treeNode.getParent();
			while (treeNode != null) {
				nodes.add(0, treeNode);
				treeNode = treeNode.getParent();
			}
		}

		return nodes.isEmpty() ? null : new TreePath(nodes.toArray());
	}

	public void cleanTree() {
		robotsNode.removeAllChildren();
		entitiesNode.removeAllChildren();

		jtreeModel.nodeChanged(robotsNode);
		jtreeModel.nodeChanged(entitiesNode);
		jtreeModel.reload();
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Teste");
		frame.getContentPane().setLayout(new BorderLayout());

		InformationTree tree = new InformationTree();
		frame.add(tree, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 400);
		frame.setVisible(true);

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		int i = 0;
		while (i < 3) {
			LinkedList<Waypoint> wps = new LinkedList<Waypoint>();
			wps.add(new Waypoint("wp1", new LatLon(20 - i, 10 - i)));
			wps.add(new Waypoint("wp2", new LatLon(30 - i, 20 - i)));
			wps.add(new Waypoint("wp3", new LatLon(40 - i, 30 - i)));
			wps.add(new Waypoint("wp4", new LatLon(50 - i, 40 - i)));

			GeoFence entity = new GeoFence("Formation " + i, wps);
			entity.setTimestepReceived(i - 10);

			ArrayList<Entity> entities = new ArrayList<Entity>();
			entities.add(entity);
			EntityManipulation manipulation = new EntityManipulation(Operation.MOVE, entities,
					entity.getClass().getSimpleName(), 100 - i);

			tree.updateEntity(manipulation);
			System.out.println("Added " + i);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			i++;
		}

		i = 0;
		while (i < 3) {
			LinkedList<Waypoint> wps = new LinkedList<Waypoint>();
			wps.add(new Waypoint("wp1", new LatLon(50 + i, 40 + i)));
			wps.add(new Waypoint("wp2", new LatLon(40 + i, 30 + i)));
			wps.add(new Waypoint("wp3", new LatLon(30 + i, 20 + i)));
			wps.add(new Waypoint("wp4", new LatLon(20 + i, 10 + i)));

			GeoFence entity = new GeoFence("Formation " + i, wps);
			entity.setTimestepReceived(i - 10);

			ArrayList<Entity> entities = new ArrayList<Entity>();
			entities.add(entity);
			EntityManipulation manipulation = new EntityManipulation(Operation.MOVE, entities,
					entity.getClass().getSimpleName(), 100 - i);

			tree.updateEntity(manipulation);
			System.out.println("Moved " + i);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			i++;
		}

		tree.cleanTree();
	}
}
