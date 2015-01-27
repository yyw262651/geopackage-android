package mil.nga.giat.geopackage.test.data.c4;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.List;

import junit.framework.TestCase;
import mil.nga.giat.geopackage.GeoPackage;
import mil.nga.giat.geopackage.data.c3.GeometryColumns;
import mil.nga.giat.geopackage.data.c3.GeometryColumnsDao;
import mil.nga.giat.geopackage.data.c4.FeatureColumn;
import mil.nga.giat.geopackage.data.c4.FeatureColumns;
import mil.nga.giat.geopackage.data.c4.FeatureCursor;
import mil.nga.giat.geopackage.data.c4.FeatureDao;
import mil.nga.giat.geopackage.data.c4.FeatureRow;
import mil.nga.giat.geopackage.geom.GeoPackageGeometry;
import mil.nga.giat.geopackage.geom.GeoPackageGeometryCollection;
import mil.nga.giat.geopackage.geom.GeoPackageGeometryData;
import mil.nga.giat.geopackage.geom.GeoPackageLineString;
import mil.nga.giat.geopackage.geom.GeoPackageMultiLineString;
import mil.nga.giat.geopackage.geom.GeoPackageMultiPoint;
import mil.nga.giat.geopackage.geom.GeoPackageMultiPolygon;
import mil.nga.giat.geopackage.geom.GeoPackagePoint;
import mil.nga.giat.geopackage.geom.GeoPackagePolygon;
import mil.nga.giat.geopackage.geom.GeometryType;
import mil.nga.giat.geopackage.geom.wkb.WkbGeometryReader;
import mil.nga.giat.geopackage.util.ByteReader;
import mil.nga.giat.geopackage.util.GeoPackageException;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;

/**
 * Features Utility test methods
 * 
 * @author osbornb
 */
public class FeatureUtils {

	private static final double POINT_UPDATED_X = 45.11111;
	private static final double POINT_UPDATED_Y = 89.99999;
	private static final double POINT_UPDATED_Z = 10.55555;
	private static final double POINT_UPDATED_M = 2.87878;

	/**
	 * Test read
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testRead(GeoPackage geoPackage) throws SQLException {

		GeometryColumnsDao geometryColumnsDao = geoPackage
				.getGeometryColumnsDao();
		List<GeometryColumns> results = geometryColumnsDao.queryForAll();

		for (GeometryColumns geometryColumns : results) {

			// Test the get feature DAO methods
			FeatureDao dao = geoPackage.getFeatureDao(geometryColumns);
			TestCase.assertNotNull(dao);
			dao = geoPackage.getFeatureDao(geometryColumns.getContents());
			TestCase.assertNotNull(dao);
			dao = geoPackage.getFeatureDao(geometryColumns.getTableName());
			TestCase.assertNotNull(dao);

			TestCase.assertNotNull(dao.getDb());
			TestCase.assertEquals(geometryColumns.getId(), dao
					.getGeometryColumns().getId());
			TestCase.assertEquals(geometryColumns.getTableName(),
					dao.getTableName());
			TestCase.assertEquals(geometryColumns.getColumnName(),
					dao.getGeometryColumnName());

			FeatureColumns featureColumns = dao.getColumns();
			String[] columns = featureColumns.getColumnNames();
			int geomIndex = featureColumns.getGeometryIndex();
			TestCase.assertTrue(geomIndex >= 0 && geomIndex < columns.length);
			TestCase.assertEquals(geometryColumns.getColumnName(),
					columns[geomIndex]);

			FeatureCursor cursor = dao.queryForAll();
			int count = cursor.getCount();
			int manualCount = 0;
			while (cursor.moveToNext()) {
				GeoPackageGeometryData geoPackageGeometryData = cursor
						.getGeometry();
				if (cursor.getBlob(featureColumns.getGeometryIndex()) != null) {
					TestCase.assertNotNull(geoPackageGeometryData);
					GeoPackageGeometry geometry = geoPackageGeometryData
							.getGeometry();
					GeometryType geometryType = geometryColumns
							.getGeometryType();
					validateGeometry(geometryType, geometry);

					byte[] wkbBytes = geoPackageGeometryData.getWkbBytes();
					int byteLenth = wkbBytes.length;
					TestCase.assertTrue(byteLenth > 0);
					ByteReader wkbReader = new ByteReader(wkbBytes);
					wkbReader.setByteOrder(geoPackageGeometryData
							.getByteOrder());
					GeoPackageGeometry geometryFromBytes = WkbGeometryReader
							.readGeometry(wkbReader);
					TestCase.assertNotNull(geometryFromBytes);
					TestCase.assertEquals(geometry.getGeometryType(),
							geometryFromBytes.getGeometryType());
					validateGeometry(geometryType, geometryFromBytes);

					ByteBuffer wkbByteBuffer = geoPackageGeometryData
							.getWkbByteBuffer();
					TestCase.assertEquals(byteLenth, wkbByteBuffer.remaining());
					byte[] wkbBytes2 = new byte[wkbByteBuffer.remaining()];
					wkbByteBuffer.get(wkbBytes2);
					ByteReader wkbReader2 = new ByteReader(wkbBytes2);
					wkbReader2.setByteOrder(geoPackageGeometryData
							.getByteOrder());
					GeoPackageGeometry geometryFromBytes2 = WkbGeometryReader
							.readGeometry(wkbReader2);
					TestCase.assertNotNull(geometryFromBytes2);
					TestCase.assertEquals(geometry.getGeometryType(),
							geometryFromBytes2.getGeometryType());
					validateGeometry(geometryType, geometryFromBytes2);
				}

				FeatureRow featureRow = cursor.getRow();
				validateFeatureRow(columns, featureRow);

				manualCount++;
			}
			TestCase.assertEquals(count, manualCount);
			cursor.close();

			cursor = (FeatureCursor) dao.getDb().query(dao.getTableName(),
					null, null, null, null, null, null);
			count = cursor.getCount();
			manualCount = 0;
			while (cursor.moveToNext()) {
				GeoPackageGeometryData geometry = cursor.getGeometry();
				if (cursor.getBlob(featureColumns.getGeometryIndex()) != null) {
					TestCase.assertNotNull(geometry);
				}
				manualCount++;
			}
			TestCase.assertEquals(count, manualCount);
			cursor.close();
			// TODO
		}

	}

	/**
	 * Validate a feature row
	 * 
	 * @param columns
	 * @param featureRow
	 */
	private static void validateFeatureRow(String[] columns,
			FeatureRow featureRow) {
		TestCase.assertEquals(columns.length, featureRow.count());

		for (int i = 0; i < featureRow.count(); i++) {
			TestCase.assertEquals(columns[i], featureRow.getName(i));
			TestCase.assertEquals(i, featureRow.getIndex(columns[i]));
			int rowType = featureRow.getRowColumnType(i);
			Object value = featureRow.getValue(i);

			switch (rowType) {

			case Cursor.FIELD_TYPE_INTEGER:
				TestCase.assertTrue(value instanceof Long);
				break;

			case Cursor.FIELD_TYPE_FLOAT:
				TestCase.assertTrue(value instanceof Double);
				break;

			case Cursor.FIELD_TYPE_STRING:
				TestCase.assertTrue(value instanceof String);
				break;

			case Cursor.FIELD_TYPE_BLOB:
				if (featureRow.getGeometryIndex() == i) {
					TestCase.assertTrue(value instanceof GeoPackageGeometryData);
				} else {
					TestCase.assertTrue(value instanceof byte[]);
				}
				break;

			case Cursor.FIELD_TYPE_NULL:
				TestCase.assertNull(value);
				break;

			}
		}
	}

	/**
	 * Validate the geometry
	 * 
	 * @param geometryType
	 * @param geometry
	 */
	private static void validateGeometry(GeometryType geometryType,
			GeoPackageGeometry geometry) {

		switch (geometryType) {
		case POINT:
			TestCase.assertTrue(geometry instanceof GeoPackagePoint);
			GeoPackagePoint point = (GeoPackagePoint) geometry;
			validatePoint(point, point);
			break;
		case LINESTRING:
			TestCase.assertTrue(geometry instanceof GeoPackageLineString);
			GeoPackageLineString lineString = (GeoPackageLineString) geometry;
			validateLineString(lineString, lineString);
			break;
		case POLYGON:
			TestCase.assertTrue(geometry instanceof GeoPackagePolygon);
			GeoPackagePolygon polygon = (GeoPackagePolygon) geometry;
			validatePolygon(polygon, polygon);
			break;
		case MULTIPOINT:
			TestCase.assertTrue(geometry instanceof GeoPackageMultiPoint);
			GeoPackageMultiPoint multiPoint = (GeoPackageMultiPoint) geometry;
			validateMultiPoint(multiPoint, multiPoint);
			break;
		case MULTILINESTRING:
			TestCase.assertTrue(geometry instanceof GeoPackageMultiLineString);
			GeoPackageMultiLineString multiLineString = (GeoPackageMultiLineString) geometry;
			validateMultiLineString(multiLineString, multiLineString);
			break;
		case MULTIPOLYGON:
			TestCase.assertTrue(geometry instanceof GeoPackageMultiPolygon);
			GeoPackageMultiPolygon multiPolygon = (GeoPackageMultiPolygon) geometry;
			validateMultiPolygon(multiPolygon, multiPolygon);
			break;
		case GEOMETRYCOLLECTION:
			TestCase.assertTrue(geometry instanceof GeoPackageGeometryCollection);
			GeoPackageGeometryCollection<GeoPackageGeometry> geometryCollection = (GeoPackageGeometryCollection<GeoPackageGeometry>) geometry;
			validateGeometryCollection(geometryCollection, geometryCollection);
			break;
		default:

		}
	}

	/**
	 * Validate Z and M values
	 * 
	 * @param topGeometry
	 * @param geometry
	 */
	private static void validateZAndM(GeoPackageGeometry topGeometry,
			GeoPackageGeometry geometry) {
		TestCase.assertEquals(topGeometry.hasZ(), geometry.hasZ());
		TestCase.assertEquals(topGeometry.hasM(), geometry.hasM());
	}

	/**
	 * Validate Point
	 * 
	 * @param topGeometry
	 * @param point
	 */
	private static void validatePoint(GeoPackageGeometry topGeometry,
			GeoPackagePoint point) {

		TestCase.assertEquals(GeometryType.POINT, point.getGeometryType());

		validateZAndM(topGeometry, point);

		if (topGeometry.hasZ()) {
			TestCase.assertNotNull(point.getZ());
		} else {
			TestCase.assertNull(point.getZ());
		}

		if (topGeometry.hasM()) {
			TestCase.assertNotNull(point.getM());
		} else {
			TestCase.assertNull(point.getM());
		}
	}

	/**
	 * Validate Line String
	 * 
	 * @param topGeometry
	 * @param lineString
	 */
	private static void validateLineString(GeoPackageGeometry topGeometry,
			GeoPackageLineString lineString) {

		TestCase.assertEquals(GeometryType.LINESTRING,
				lineString.getGeometryType());

		validateZAndM(topGeometry, lineString);

		for (GeoPackagePoint point : lineString.getPoints()) {
			validatePoint(topGeometry, point);
		}

	}

	/**
	 * Validate Polygon
	 * 
	 * @param topGeometry
	 * @param polygon
	 */
	private static void validatePolygon(GeoPackageGeometry topGeometry,
			GeoPackagePolygon polygon) {

		TestCase.assertEquals(GeometryType.POLYGON, polygon.getGeometryType());

		validateZAndM(topGeometry, polygon);

		for (GeoPackageLineString ring : polygon.getRings()) {
			validateLineString(topGeometry, ring);
		}

	}

	/**
	 * Validate Multi Point
	 * 
	 * @param topGeometry
	 * @param multiPoint
	 */
	private static void validateMultiPoint(GeoPackageGeometry topGeometry,
			GeoPackageMultiPoint multiPoint) {

		TestCase.assertEquals(GeometryType.MULTIPOINT,
				multiPoint.getGeometryType());

		validateZAndM(topGeometry, multiPoint);

		for (GeoPackagePoint point : multiPoint.get()) {
			validatePoint(topGeometry, point);
		}

	}

	/**
	 * Validate Multi Line String
	 * 
	 * @param topGeometry
	 * @param multiLineString
	 */
	private static void validateMultiLineString(GeoPackageGeometry topGeometry,
			GeoPackageMultiLineString multiLineString) {

		TestCase.assertEquals(GeometryType.MULTILINESTRING,
				multiLineString.getGeometryType());

		validateZAndM(topGeometry, multiLineString);

		for (GeoPackageLineString lineString : multiLineString.get()) {
			validateLineString(topGeometry, lineString);
		}

	}

	/**
	 * Validate Multi Polygon
	 * 
	 * @param topGeometry
	 * @param multiPolygon
	 */
	private static void validateMultiPolygon(GeoPackageGeometry topGeometry,
			GeoPackageMultiPolygon multiPolygon) {

		TestCase.assertEquals(GeometryType.MULTIPOLYGON,
				multiPolygon.getGeometryType());

		validateZAndM(topGeometry, multiPolygon);

		for (GeoPackagePolygon polygon : multiPolygon.get()) {
			validatePolygon(topGeometry, polygon);
		}

	}

	/**
	 * Validate Geometry Collection
	 * 
	 * @param topGeometry
	 * @param geometryCollection
	 */
	private static void validateGeometryCollection(
			GeoPackageGeometry topGeometry,
			GeoPackageGeometryCollection<GeoPackageGeometry> geometryCollection) {

		validateZAndM(topGeometry, geometryCollection);

		for (GeoPackageGeometry geometry : geometryCollection.get()) {
			validateGeometry(geometry.getGeometryType(), geometry);
		}

	}

	/**
	 * Test update
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testUpdate(GeoPackage geoPackage) throws SQLException {

		GeometryColumnsDao geometryColumnsDao = geoPackage
				.getGeometryColumnsDao();
		List<GeometryColumns> results = geometryColumnsDao.queryForAll();

		for (GeometryColumns geometryColumns : results) {

			FeatureDao dao = geoPackage.getFeatureDao(geometryColumns);
			TestCase.assertNotNull(dao);

			FeatureCursor cursor = dao.queryForAll();
			int count = cursor.getCount();
			if (count > 0) {

				// // Choose random feature
				// int random = (int) (Math.random() * count);
				// cursor.moveToPosition(random);
				cursor.moveToFirst();

				GeoPackageGeometryData geometryData = cursor.getGeometry();
				while (geometryData == null && cursor.moveToNext()) {
					geometryData = cursor.getGeometry();
				}
				if (geometryData != null) {
					String UPDATED_STRING = "updated string";

					GeoPackageGeometry geometry = geometryData.getGeometry();
					FeatureRow originalRow = cursor.getRow();
					FeatureRow featureRow = cursor.getRow();

					try {
						featureRow.setValue(featureRow.getPkIndex(), 9);
						TestCase.fail("Updated the primary key value");
					} catch (GeoPackageException e) {
						// expected
					}

					for (FeatureColumn featureColumn : dao.getColumns()
							.getColumns()) {
						if (!featureColumn.isPrimaryKey()) {

							if (featureColumn.isGeometry()) {

								GeoPackageGeometryData updatedGeometryData = geometryData;

								switch (geometry.getGeometryType()) {

								case POINT:
									GeoPackagePoint point = (GeoPackagePoint) geometry;
									updatePoint(point);
									break;
								case MULTIPOINT:
									GeoPackageMultiPoint multiPoint = (GeoPackageMultiPoint) geometry;
									if (multiPoint.count() > 1) {
										multiPoint.get().remove(0);
									}
									for (GeoPackagePoint multiPointPoint : multiPoint
											.get()) {
										updatePoint(multiPointPoint);
									}
									break;

								default:
									updatedGeometryData = null;
								}
								if (updatedGeometryData != null) {
									featureRow.setValue(
											featureColumn.getIndex(),
											updatedGeometryData);
								}

							} else {
								switch (featureRow
										.getRowColumnType(featureColumn
												.getIndex())) {

								case Cursor.FIELD_TYPE_STRING:
									featureRow.setValue(
											featureColumn.getIndex(),
											UPDATED_STRING);
									break;

								default:
								}
							}

						}
					}

					cursor.close();
					try {
						TestCase.assertEquals(1, dao.update(featureRow));
					} catch (SQLiteException e) {
						// Example files using sqlite geopackage functions from
						// 4.2.0 will not support updates, lollipop uses 3.8.4.3
						if (e.getMessage().contains(
								"no such function: ST_IsEmpty")) {
							continue;
						} else {
							throw e;
						}
					}

					int id = featureRow.getId();
					FeatureRow readRow = dao.queryForIdRow(id);
					TestCase.assertNotNull(readRow);
					TestCase.assertEquals(originalRow.getId(), readRow.getId());
					GeoPackageGeometryData readGeometryData = readRow
							.getGeometry();
					GeoPackageGeometry readGeometry = readGeometryData
							.getGeometry();

					for (String readColumnName : readRow.getNames()) {

						FeatureColumn readFeatureColumn = readRow
								.getColumn(readColumnName);
						if (!readFeatureColumn.isPrimaryKey()
								&& !readFeatureColumn.isGeometry()) {
							switch (readRow.getRowColumnType(readColumnName)) {
							case Cursor.FIELD_TYPE_STRING:
								TestCase.assertEquals(UPDATED_STRING, readRow
										.getValue(readFeatureColumn.getIndex()));
								break;

							default:
							}
						}

					}

					switch (geometry.getGeometryType()) {

					case POINT:
						GeoPackagePoint point = (GeoPackagePoint) readGeometry;
						validateUpdatedPoint(point);
						break;

					case MULTIPOINT:
						GeoPackageMultiPoint originalMultiPoint = (GeoPackageMultiPoint) geometry;
						GeoPackageMultiPoint multiPoint = (GeoPackageMultiPoint) readGeometry;
						TestCase.assertEquals(originalMultiPoint.count(),
								multiPoint.count());
						for (GeoPackagePoint multiPointPoint : multiPoint.get()) {
							validateUpdatedPoint(multiPointPoint);
						}
						break;

					default:
						geometry.getGeometryType();
					}

				}

			}
			cursor.close();
		}

	}

	private static void updatePoint(GeoPackagePoint point) {
		point.setX(POINT_UPDATED_X);
		point.setY(POINT_UPDATED_Y);
		if (point.hasZ()) {
			point.setZ(POINT_UPDATED_Z);
		}
		if (point.hasM()) {
			point.setM(POINT_UPDATED_M);
		}
	}

	private static void validateUpdatedPoint(GeoPackagePoint point) {
		TestCase.assertEquals(POINT_UPDATED_X, point.getX());
		TestCase.assertEquals(POINT_UPDATED_Y, point.getY());
		if (point.hasZ()) {
			TestCase.assertEquals(POINT_UPDATED_Z, point.getZ());
		}
		if (point.hasM()) {
			TestCase.assertEquals(POINT_UPDATED_M, point.getM());
		}
	}

	/**
	 * Test create
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testCreate(GeoPackage geoPackage) throws SQLException {

		// TODO

	}

	/**
	 * Test delete
	 * 
	 * @param geoPackage
	 * @throws SQLException
	 */
	public static void testDelete(GeoPackage geoPackage) throws SQLException {

		// TODO

	}

}