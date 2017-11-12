package fiware.smartcity.render;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapOverlayType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import fiware.smartcity.Application;
import fiware.smartcity.R;
import fiware.smartcity.Utilities;
import fiware.smartcity.ngsi.Entity;

/**
 *
 * Renders extra objects such as parking restrictions, gas stations or garages
 *
 *
 */
public class ExtraObjRenderer {
    private static List<String> supportedTypes = new ArrayList<>(
            Arrays.asList(Application.GAS_STATION_TYPE, Application.GARAGE_TYPE, Application.POI_TYPE,
                    Application.BIKE_HIRE_DOCK_TYPE, Application.TRAFFIC_CAMERA_TYPE, Application.TRAFFIC_ISSUE_TYPE));


    public static void render(Context ctx, Map map, java.util.Map<String, List<Entity>> entities) {

        for (String type : supportedTypes) {
            List<Entity> typeEntities = entities.get(type);

            if (typeEntities != null) {
                for (Entity typeEntity: typeEntities) {
                    if (Application.renderedEntities.get(typeEntity.id) != null) {
                        continue;
                    }

                    renderEntity(type, ctx, map, typeEntity);
                }
            }
        }
    }

    private static void renderEntity(String type, Context ctx, Map map, Entity ent) {
        if (type.equals(Application.GAS_STATION_TYPE)) {
            renderGasStation(ctx, map, ent);

        } else if (type.equals(Application.GARAGE_TYPE)) {
            renderGarage(ctx, map, ent);

        } else if (type.equals(Application.POI_TYPE)) {
            renderPoi(ctx, map, ent);

        } else if (type.equals(Application.BIKE_HIRE_DOCK_TYPE)) {
            renderBikeHireDock(ctx, map, ent);

        } else if (type.equals(Application.TRAFFIC_CAMERA_TYPE)) {
            renderTrafficCamera(ctx, map, ent);

        } else if (type.equals(Application.TRAFFIC_ISSUE_TYPE)) {
            renderTrafficIssue(ctx, map, ent);
        }
    }

    private static void renderPointName(Context ctx, Map map, Entity ent, int icon) {
        renderPoint(ctx, map, ent, icon, (String) ent.attributes.get("name"));
    }

    private static void renderPoint(Context ctx, Map map, Entity ent, int icon, String name) {
        GeoCoordinate coords = new GeoCoordinate(ent.location[0], ent.location[1]);

        RenderStyle style = new RenderStyle();
        style.textColor =  Color.BLACK;
        style.textStyle = Typeface.BOLD;

        MapMarker mapMarker = new MapMarker(coords,
                RenderUtilities.createLabeledIcon(ctx,
                        name, style,
                        icon
                        ));

        mapMarker.setOverlayType(MapOverlayType.FOREGROUND_OVERLAY);
        map.addMapObject(mapMarker);

        Application.mapObjects.add(mapMarker);

        Application.renderedEntities.put(ent.id, ent.id);
    }

    private static void renderGasStation(Context ctx, Map map, Entity ent) {
        renderPointName(ctx, map, ent, R.drawable.gas_station);
    }

    private static void renderGarage(Context ctx, Map map, Entity ent) {
        renderPointName(ctx, map, ent, R.drawable.car_repair);
    }

    private static void renderPoi(Context ctx, Map map, Entity ent) {
        String category = ((List<String>) ent.attributes.get("category")).get(0);
        int icon = Utilities.getPOIMarker(category);
        renderPointName(ctx, map, ent, icon);

    }

    private static void renderBikeHireDock(Context ctx, Map map, Entity ent) {
        String name = ent.attributes.get("availableBikeNumber") + " / " + ent.attributes.get("totalSlotNumber");
        renderPoint(ctx, map, ent, R.drawable.bike, name);
    }

    private static void renderTrafficCamera(Context ctx, Map map, Entity ent) {
        renderPointName(ctx, map, ent, R.drawable.camera);
    }

    private static void renderTrafficIssue(Context ctx, Map map, Entity ent) {
        String category = "leisureEvent"; // Default one
        List<String> categories = (List<String>) ent.attributes.get("category");

        if (categories.size() > 0) {
            category = categories.contains("accident") ? "accident" : categories.get(0);
        }

        int icon = Utilities.getIssueMarker(category);
        String name = (String) ent.attributes.get("description");

        if (name.length() > 20) {
            name = name.substring(0, 20);
            name += "...";
        }

        renderPoint(ctx, map, ent, icon, name);
    }
}
