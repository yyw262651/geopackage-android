package mil.nga.geopackage.extension.properties;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.attributes.AttributesCursor;
import mil.nga.geopackage.attributes.AttributesDao;
import mil.nga.geopackage.attributes.AttributesRow;

/**
 * GeoPackage properties extension for defining GeoPackage specific properties,
 * attributes, and metadata
 *
 * @author osbornb
 * @since 3.0.2
 */
public class PropertiesExtension
        extends
        PropertiesCoreExtension<AttributesRow, AttributesCursor, AttributesDao> {

    /**
     * GeoPackage
     */
    private final GeoPackage geoPackage;

    /**
     * Constructor
     *
     * @param geoPackage GeoPackage
     */
    public PropertiesExtension(GeoPackage geoPackage) {
        super(geoPackage);
        this.geoPackage = geoPackage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AttributesDao getDao() {
        return geoPackage.getAttributesDao(TABLE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AttributesRow newRow() {
        return getDao().newRow();
    }

}
