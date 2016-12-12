package gui.map;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.Layer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.OsmFileCacheTileLoader;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.Style;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import commoninterface.entities.Entity;
import commoninterface.entities.GeoEntity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.ObstacleLocation;
import commoninterface.entities.RobotLocation;
import commoninterface.entities.Waypoint;
import commoninterface.entities.target.Formation;
import commoninterface.entities.target.Target;
import commoninterface.utils.jcoord.LatLon;
import gui.SpringUtilities;
import gui.map.markers.MapMarkerBaseStation;
import gui.map.markers.MapMarkerDrone;
import gui.map.markers.MapMarkerObstacle;
import gui.map.markers.MapMarkerWaypoint;

public class MapPanel extends JComponent {
	private static final long serialVersionUID = 8239117061075444973L;
	private static String TILES_FOLDER_PATH = "C:\\Users\\BIOMACHINES\\Desktop\\Eclipse Data\\Drones Software\\DroneControlConsole\\tiles";
	private static Coordinate LISBON_COORDINATES = new Coordinate(38.7166700, -9.1333300);
	private static int POSITION_HISTORY = 1;

	private JMapViewerImplementation mapInstance = null;

	/*
	 * Entities specific variables
	 */
	// Robots
	private HashMap<String, LinkedList<MapMarker>> robotPositions = new HashMap<String, LinkedList<MapMarker>>();
	private HashMap<String, Long> robotPositionsLastUpdate = new HashMap<String, Long>();
	private int robotMarkerIndex = 0;

	private MapMarker basestationMarker = null;

	// Geofence
	private GeoFence geoFence = null;
	private MapPolygonImpl geoFenceMapPolygon = null;
	private LinkedList<MapMarker> geoFenceMarkers = new LinkedList<MapMarker>();

	// Formation
	private Formation formation = null;
	private MapMarker formationCenterMarker = null;
	private LinkedList<Target> formationTargets = new LinkedList<Target>();
	private LinkedList<MapMarker> formationTargetMarkers = new LinkedList<MapMarker>();

	// Targets
	private LinkedList<Target> targets = new LinkedList<Target>();
	private LinkedList<MapMarker> targetMarkers = new LinkedList<MapMarker>();

	// Waypoints
	private LinkedList<Waypoint> waypoints = new LinkedList<Waypoint>();
	private LinkedList<MapMarker> waypointMarkers = new LinkedList<MapMarker>();

	// Obstacles
	private LinkedList<ObstacleLocation> obstacles = new LinkedList<ObstacleLocation>();
	private LinkedList<MapMarker> obstacleMarkers = new LinkedList<MapMarker>();

	public MapPanel() {
		setLayout(new BorderLayout());

		/*
		 * Create map instance
		 */
		mapInstance = new JMapViewerImplementation("Zones",true);
		mapInstance.getViewer().setDisplayPosition(LISBON_COORDINATES, 13);
		add(mapInstance, BorderLayout.CENTER);

		/*
		 * Create top JComboBox
		 */
		JPanel topPanel = new JPanel(new BorderLayout());
		add(topPanel, BorderLayout.NORTH);

		try {
			JComboBox<TileSource> tileSourceSelector = new JComboBox<>(new TileSource[] {
					new OfflineOsmTileSource((new File(TILES_FOLDER_PATH).toURI().toURL()).toString(), 1, 19),
					new OsmTileSource.Mapnik(), new BingAerialTileSource() });
			tileSourceSelector.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					getMap().setTileSource((TileSource) e.getItem());
				}
			});

			getMap().setTileSource(tileSourceSelector.getItemAt(0));

			JPanel topRightPanel = new JPanel(new SpringLayout());
			topRightPanel.add(new JLabel("Tiles source:"));
			topRightPanel.add(tileSourceSelector);
			SpringUtilities.makeCompactGrid(topRightPanel, 1, 2, 0, 0, 10, 0);
			topPanel.add(topRightPanel, BorderLayout.EAST);

			try {
				getMap().setTileLoader(new OsmFileCacheTileLoader(getMap()));
			} catch (IOException e) {
				getMap().setTileLoader(new OsmTileLoader(getMap()));
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		initActions();
	}

	/*
	 * GUI stuff
	 */
	protected void initActions() {
		// Zoom in
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('+'), "+");
		getActionMap().put("+", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				getMap().zoomIn();
			}
		});

		// Zoom out
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('-'), "-");
		getActionMap().put("-", new AbstractAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				getMap().zoomOut();
			}
		});

		// Fit existing markers
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F"), "F");
		getActionMap().put("F", new AbstractAction() {
			protected static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				getMap().setDisplayToFitMapMarkers();
			}
		});
	}

	public synchronized void updateRobotPosition(RobotLocation di) {
		double lat = di.getLatLon().getLat();
		double lon = di.getLatLon().getLon();

		if (lat != 0 && lon != 0) {
			double orientation = di.getOrientation();
			String name = di.getName().isEmpty() ? "drone" : di.getName();

			LinkedList<MapMarker> robotMarkers = robotPositions.get(name);
			if (robotMarkers == null) {
				robotMarkers = new LinkedList<MapMarker>();
				robotPositions.put(name, robotMarkers);
				robotPositionsLastUpdate.put(name, System.currentTimeMillis());
			}

			Layer layer = null;
			synchronized (this) {
				Iterator<MapMarker> i = getMap().getMapMarkerList().iterator();

				while (i.hasNext()) {
					MapMarker m = i.next();
					if (m.getLayer() != null && m.getLayer().getName().equals(name)) {
						layer = m.getLayer();
						break;
					}
				}
			}

			if (layer == null) {
				layer = mapInstance.addLayer(name);
			}

			// Create a position history in the list
			if (!robotMarkers.isEmpty()) {
				Style styleOld = new Style(Color.BLACK, Color.LIGHT_GRAY, new BasicStroke(1),
						new Font("Dialog", Font.PLAIN, 0));

				// Remove last value from previous iteration
				MapMarker last = robotMarkers.pollLast();
				mapInstance.removeFromLayer(last);
				getMap().removeMapMarker(last);

				// Add that same one with a different style
				MapMarker old = new MapMarkerDot(layer, "" + robotMarkerIndex++, last.getCoordinate(), styleOld);
				robotMarkers.add(old);
				layer.add(old);
				getMap().addMapMarker(old);
				robotPositionsLastUpdate.put(name, System.currentTimeMillis());

			}

			Style styleNew = null;

			switch (di.getDroneType()) {
			case DRONE:
				styleNew = new Style(Color.RED, Color.GREEN, new BasicStroke(1), new Font("Dialog", Font.PLAIN, 12));
				break;
			case ENEMY:
				styleNew = new Style(Color.RED, Color.RED, new BasicStroke(1), new Font("Dialog", Font.PLAIN, 12));
				break;
			default:
				styleNew = new Style(Color.RED, Color.GREEN, new BasicStroke(1), new Font("Dialog", Font.PLAIN, 12));
			}

			MapMarker m = new MapMarkerDrone(layer, name, latLonToCoord(lat, lon), styleNew, orientation);
			layer.add(m);
			getMap().addMapMarker(m);
			robotMarkers.add(m);

			while (robotMarkers.size() > POSITION_HISTORY) {
				MapMarker old = robotMarkers.pollFirst();
				mapInstance.removeFromLayer(old);
				getMap().removeMapMarker(old);
			}
		}
	}

	public synchronized void displayData(RobotLocation di) {
		LatLon latLon = di.getLatLon();

		if (latLon != null) {
			double lat = latLon.getLat();
			double lon = latLon.getLon();

			if (lat == 0 && lon == 0)
				return;

			if (usefulRobotCoordinate(di.getName(), latLonToCoord(lat, lon))) {
				updateRobotPosition(di);
			}
		}
	}

	public ArrayList<Entity> getEntities() {
		ArrayList<Entity> entities = new ArrayList<Entity>();

		if (!geoFence.getWaypoints().isEmpty())
			entities.add(geoFence);

		if (waypoints != null)
			entities.addAll(waypoints);

		if (obstacles != null)
			entities.addAll(obstacles);

		if (formation != null) {
			entities.add(formation);
		}

		return entities;
	}

	private JMapViewer getMap() {
		return mapInstance.getViewer();
	}

	public Formation getFormation() {
		return formation;
	}

	/*
	 * Add and replace entities to/on map
	 */
	public void addWaypoint(Waypoint wp) {
		addWaypoint(latLonToCoord(wp.getLatLon()));
	}

	public void addWaypoint(Coordinate c) {
		String layerName = "waypoints";
		Layer l = null;

		for (Layer layer : mapInstance.getLayers()) {
			if (layer.getName().equals(layerName)) {
				l = layer;
			}
		}
		if (l == null) {
			l = mapInstance.addLayer(layerName);
		}

		String markerName = "waypoint" + waypoints.size();

		MapMarker m = new MapMarkerWaypoint(l, markerName, c);

		l.add(m);
		waypointMarkers.add(m);

		getMap().addMapMarker(m);

		synchronized (this) {
			Waypoint waypoint = new Waypoint(markerName, new LatLon(c.getLat(), c.getLon()));
			waypoints.add(waypoint);
		}
	}

	public void addTarget(Coordinate c, String name, double radius) {
		String layerName = "targets";
		Layer l = null;

		for (Layer layer : mapInstance.getLayers()) {
			if (layer.getName().equals(layerName)) {
				l = layer;
			}
		}
		if (l == null) {
			l = mapInstance.addLayer(layerName);
		}

		String markerName = name.substring(name.length() - 3, name.length());
		MapMarker m = new MapMarkerWaypoint(l, markerName, c, Color.BLUE);

		l.add(m);
		targetMarkers.add(m);

		getMap().addMapMarker(m);

		synchronized (this) {
			Target t = new Target(name, new LatLon(c.getLat(), c.getLon()), radius);
			targets.add(t);
		}
	}

	public void addGeoFence(GeoFence geoFence) {
		if (geoFence != null) {
			clearGeoFence();
		}

		ArrayList<Coordinate> polygonCoordinates = new ArrayList<Coordinate>();
		this.geoFence = geoFence;
		for (Waypoint wp : geoFence.getWaypoints()) {
			Coordinate coordinates = latLonToCoord(wp.getLatLon());
			polygonCoordinates.add(coordinates);

			MapMarker marker = new MapMarkerDot(coordinates);
			geoFenceMarkers.add(marker);
			getMap().addMapMarker(marker);
		}

		geoFenceMapPolygon = new MapPolygonImpl(polygonCoordinates);
		getMap().addMapPolygon(geoFenceMapPolygon);
	}

	private void addFormationCenterMarker(Coordinate c) {
		if (formationCenterMarker != null) {
			mapInstance.removeFromLayer(formationCenterMarker);
			getMap().removeMapMarker(formationCenterMarker);
			formationCenterMarker = null;
		}

		String layerName = "formation";
		Layer layer = null;

		for (Layer l : mapInstance.getLayers()) {
			if (l.getName().equals(layerName)) {
				layer = l;
				break;
			}
		}

		if (layer == null) {
			layer = mapInstance.addLayer(layerName);
		}

		formationCenterMarker = new MapMarkerObstacle(layer, "", new Coordinate(c.getLat(), c.getLon()));
		layer.add(formationCenterMarker);
		getMap().addMapMarker(formationCenterMarker);
	}

	public void addFormation(Formation formation) {
		synchronized (this) {
			clearFormation();
			this.formation = formation;
			String layerName = "formation";
			Layer layer = null;

			for (Layer l : mapInstance.getLayers()) {
				if (l.getName().equals(layerName)) {
					layer = l;
					break;
				}
			}

			if (layer == null) {
				layer = mapInstance.addLayer(layerName);
			}

			for (Target t : formation.getTargets()) {
				Coordinate position = new Coordinate(t.getLatLon().getLat(), t.getLatLon().getLon());
				String name = t.getName().replace("formation_target_", "");

				MapMarker marker = new MapMarkerWaypoint(layer, name, position, Color.BLUE);
				layer.add(marker);
				formationTargetMarkers.add(marker);
				getMap().addMapMarker(marker);
			}

			addFormationCenterMarker(new Coordinate(formation.getLatLon().getLat(), formation.getLatLon().getLon()));
			formationTargets.addAll(formation.getTargets());
		}
	}

	public void addObstacle(Coordinate c) {
		String layerName = "obstacles";

		Layer l = null;

		for (Layer layer : mapInstance.getLayers())
			if (layer.getName().equals("obstacles"))
				l = layer;

		if (l == null) {
			l = mapInstance.addLayer(layerName);
		}

		String markerName = "obstacle" + obstacles.size();

		MapMarker m = new MapMarkerObstacle(l, markerName, c);

		l.add(m);
		obstacleMarkers.add(m);

		getMap().addMapMarker(m);

		synchronized (this) {
			ObstacleLocation ol = new ObstacleLocation(markerName, new LatLon(c.getLat(), c.getLon()));
			obstacles.add(ol);
		}
	}

	public void setBaseStation(LatLon position) {
		if (basestationMarker != null) {
			basestationMarker.setLat(position.getLat());
			basestationMarker.setLon(position.getLon());
		} else {
			String layerName = "markers";
			Layer l = null;

			for (Layer layer : mapInstance.getLayers()) {
				if (layer.getName().equals(layerName)) {
					l = layer;
				}
			}
			if (l == null) {
				l = mapInstance.addLayer(layerName);
			}

			String markerName = "basestation";
			basestationMarker = new MapMarkerBaseStation(l, markerName,
					new Coordinate(position.getLat(), position.getLon()));
			l.add(basestationMarker);
			getMap().addMapMarker(basestationMarker);
		}
	}

	public void replaceEntities(ArrayList<Entity> entities) {
		clearObstacles();
		clearGeoFence();
		clearWaypoints();
		clearFormation();
		clearTargets();

		for (Entity e : entities) {
			if (e instanceof GeoEntity) {
				GeoEntity ge = (GeoEntity) e;
				if (ge instanceof Waypoint)
					addWaypoint(latLonToCoord(ge.getLatLon()));
				if (ge instanceof ObstacleLocation)
					addObstacle(latLonToCoord(ge.getLatLon()));
				if (ge instanceof Formation)
					addFormation((Formation) ge);
				if (ge instanceof Target)
					addTarget(latLonToCoord(ge.getLatLon()), ge.getName(), ((Target) ge).getRadius());
			}
			if (e instanceof GeoFence) {
				addGeoFence((GeoFence) e);
			}
		}
	}

	/*
	 * Clears
	 */
	public void clearRobot(String name) {
		LinkedList<MapMarker> robotMarkers = robotPositions.get(name);
		Iterator<MapMarker> i = getMap().getMapMarkerList().iterator();
		Layer l = null;

		while (i.hasNext()) {
			MapMarker m = i.next();
			if (m.getLayer() != null && m.getLayer().getName().equals(name)) {
				l = m.getLayer();
				break;
			}
		}

		if (robotMarkers != null & l != null && !robotMarkers.isEmpty()) {

			i = robotMarkers.iterator();

			while (i.hasNext()) {
				MapMarker m = i.next();
				mapInstance.removeFromLayer(m);
				getMap().removeMapMarker(m);
				i.remove();
			}
		}
	}

	public void clearEntities() {
		clearObstacles();
		clearGeoFence();
		clearWaypoints();
		clearFormation();
		clearTargets();
	}

	public void clearWaypoints() {
		for (MapMarker m : waypointMarkers) {
			mapInstance.removeFromLayer(m);
			getMap().removeMapMarker(m);
		}

		waypoints.clear();
		waypointMarkers.clear();
	}

	public void clearTargets() {
		for (MapMarker m : targetMarkers) {
			mapInstance.removeFromLayer(m);
			getMap().removeMapMarker(m);
		}

		targets.clear();
		targetMarkers.clear();
	}

	public void clearFormation() {
		for (MapMarker m : formationTargetMarkers) {
			mapInstance.removeFromLayer(m);
			getMap().removeMapMarker(m);
		}

		if (formationCenterMarker != null) {
			mapInstance.removeFromLayer(formationCenterMarker);
			getMap().removeMapMarker(formationCenterMarker);
			formationCenterMarker = null;
		}

		formationTargets.clear();
		formationTargetMarkers.clear();
		formation = null;
	}

	public void clearObstacles() {
		for (MapMarker m : obstacleMarkers) {
			mapInstance.removeFromLayer(m);
			getMap().removeMapMarker(m);
		}

		obstacles.clear();
		obstacleMarkers.clear();
	}

	public void clearGeoFence() {
		for (MapMarker m : geoFenceMarkers) {
			mapInstance.removeFromLayer(m);
			getMap().removeMapMarker(m);
		}

		geoFence = null;
		geoFenceMarkers.clear();
		getMap().removeMapPolygon(geoFenceMapPolygon);
	}

	public void clearBaseStation() {
		if (basestationMarker != null) {
			mapInstance.removeFromLayer(basestationMarker);
			getMap().removeMapMarker(basestationMarker);
			basestationMarker = null;
		}
	}

	public void clearHistory() {
		for (String s : robotPositions.keySet()) {
			LinkedList<MapMarker> robotMarkers = robotPositions.get(s);

			while (!robotMarkers.isEmpty()) {
				MapMarker old = robotMarkers.pollFirst();
				mapInstance.removeFromLayer(old);
				getMap().removeMapMarker(old);
			}
		}
	}

	/*
	 * Utils
	 */
	private static Coordinate latLonToCoord(double lat, double lon) {
		return new Coordinate(lat, lon);
	}

	private static Coordinate latLonToCoord(LatLon latLon) {
		return latLonToCoord(latLon.getLat(), latLon.getLon());
	}

	private boolean usefulRobotCoordinate(String name, Coordinate n) {
		if (n.getLat() == -1 && n.getLon() == -1)
			return false;

		LinkedList<MapMarker> robotMarkers = robotPositions.get(name);
		if (robotMarkers == null || robotMarkers.isEmpty()) {
			return true;
		}

		robotPositionsLastUpdate.put(name, System.currentTimeMillis());
		Coordinate c = robotMarkers.peekLast().getCoordinate();
		if (c.getLat() == n.getLat() && c.getLon() == n.getLon())
			return false;

		return true;
	}
}
