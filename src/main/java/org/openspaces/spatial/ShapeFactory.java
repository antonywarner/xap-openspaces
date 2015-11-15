/*******************************************************************************
 *
 * Copyright (c) 2015 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package org.openspaces.spatial;

import com.gigaspaces.spatial.shapes.Circle;
import com.gigaspaces.spatial.shapes.Point;
import com.gigaspaces.spatial.shapes.Polygon;
import com.gigaspaces.spatial.shapes.Rectangle;

/**
 * Factory class for creating spatial shapes.
 *
 * @author Niv Ingberg
 * @since 11.0
 */
public class ShapeFactory {
    /**
     * Private ctor to prevent instantiating this factory class.
     */
    private ShapeFactory() {
    }

    /**
     * Creates a Point instance.
     * @param x The X coordinate, or Longitude in geospatial contexts
     * @param y The Y coordinate, or Latitude in geospatial contexts
     * @return A new Point instance
     */
    public static Point point(double x, double y) {
        return new Point(x, y);
    }

    /**
     * Creates a Circle instance
     * @param center The center of the circle
     * @param radius The radius of the circle
     * @return A new Circle instance
     */
    public static Circle circle(Point center, double radius) {
        return new Circle(center, radius);
    }

    /**
     * Creates a Rectangle instance
     * @param minX The left edge of the X coordinate
     * @param maxX The right edge of the X coordinate
     * @param minY The bottom edge of the Y coordinate
     * @param maxY The top edge of the Y coordinate
     * @return A new Rectangle instance
     */
    public static Rectangle rectangle(double minX, double maxX, double minY, double maxY) {
        return new Rectangle(minX, maxX, minY, maxY);
    }

    /**
     * Creates a Polygon instance
     * @param first The first point
     * @param second The second point
     * @param third The third point
     * @param morePoints The rest of the points
     * @return A new Polygon instance
     */
    public static Polygon polygon(Point first, Point second, Point third, Point... morePoints) {
        return new Polygon(first, second, third, morePoints);
    }
}