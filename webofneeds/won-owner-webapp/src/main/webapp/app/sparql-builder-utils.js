/**
 * Module for utility-functions for string-building sparql-queries.
 * NOTE: This is a super-hacky/-fragile approach and should be replaced by a proper lib / ast-utils
 */

import won from "./won-es6.js";
import { isValidNumber, isValidDate, toLocalISODateString } from "./utils.js";

/**
 * returns e.g.:
 * ```
 * {
 *   prefixes: { s: "http://schema.org/", ...},
 *   filterStrings: [ "FILTER (?currency = 'EUR')",... ],
 *   basicGraphPattern: [ "?is s:priceSpecification ?pricespec .", ...],
 * }
 * ```
 * The defaults are empty objects and arrays as properties.
 *
 * @param {*} returnValue
 */
function wellFormedFilterReturn(returnValue) {
  return Object.assign(
    {
      prefixes: {},
      filterStrings: [],
      basicGraphPattern: [],
    },
    returnValue
  );
}

/**
 * Concatenates filter-objects generated by other functions in this
 * module into a single one.
 * @param {*} filters see `wellFormedFilterReturn` for the returned structure.
 */
export function concatenateFilters(filters) {
  const concatenatedFilter = filters.reduce((acc, f) => {
    if (!f) {
      return acc;
    } else {
      const prefixes = Object.assign({}, acc.prefixes, f.prefixes);
      const filterStrings = acc.filterStrings.concat(f.filterStrings);
      const basicGraphPattern = acc.basicGraphPattern.concat(
        f.basicGraphPattern
      );
      return {
        prefixes,
        filterStrings,
        basicGraphPattern,
      };
    }
  }, wellFormedFilterReturn());

  return concatenatedFilter;
}
/**
 * @param {String} rootSubject: a variable name via which the location is connected
 *  to the rest of the graph-patterns . e.g. `"?location"`. Needs to start with a
 *  variable indicator (i.e. `?`) as other variable names will be derived by
 *  suffixing it.
 * @param {*} location: an object containing `lat` and `lng`
 * @param {Number} radius: distance in km that matches can be away from the location
 * @returns see wellFormedFilterReturn
 */
export function filterInVicinity(rootSubject, location, radius = 10) {
  if (!location || !location.lat || !location.lng) {
    return wellFormedFilterReturn();
  } else {
    /* "prefix" variable name with root-subject so filter can be used 
     * multiple times for different roots
     * results in e.g. `?location_geo`
     */
    const geoVar = `${rootSubject}_geo`;
    return wellFormedFilterReturn({
      prefixes: {
        s: won.defaultContext["s"],
        won: won.defaultContext["won"],
        geo: "http://www.bigdata.com/rdf/geospatial#",
        geoliteral: "http://www.bigdata.com/rdf/geospatial/literals/v1#",
      },
      basicGraphPattern: [`${rootSubject} s:geo ${geoVar}.`],
      filterStrings: [
        `SERVICE geo:search {
  ${geoVar} geo:search "inCircle" .
  ${geoVar} geo:searchDatatype geoliteral:lat-lon .
  ${geoVar} geo:predicate won:geoSpatial .
  ${geoVar} geo:spatialCircleCenter "${location.lat}#${location.lng}" .
  ${geoVar} geo:spatialCircleRadius "${radius}" .
  ${geoVar} geo:distanceValue ${rootSubject}_geoDistance .
}`,
      ],
    });
  }
}

/**
 * Constructs a filter for which holds:
 *
 * `datetime - 12h <= matchedTime <= datetime + 12h`
 *
 * @param {*} rootSubject the sparql-variable that is the `s:DateTime`
 * @param {*} datetime the datetime around which to construct a filter-bracket
 * @param {*} hoursBeforeAndAfter
 */
export function filterAboutTime(
  rootSubject,
  datetime,
  hoursBeforeAndAfter = 12
) {
  if (!isValidDate(datetime)) {
    return wellFormedFilterReturn();
  } else {
    const min = new Date(datetime);
    min.setHours(min.getHours() - hoursBeforeAndAfter);
    const minStr = toLocalISODateString(min);
    const max = new Date(datetime);
    max.setHours(max.getHours() + hoursBeforeAndAfter);
    const maxStr = toLocalISODateString(max);

    return wellFormedFilterReturn({
      prefixes: {
        s: won.defaultContext["s"],
      },
      basicGraphPattern: [],
      filterStrings: [
        `FILTER (${rootSubject} >= ${minStr} )`,
        `FILTER (${rootSubject} <= ${maxStr} )`,
      ],
    });
  }
}

export function filterFloorSizeRange(rootSubject, min, max) {
  const basicGraphPattern = [];
  const filterStrings = [];
  const prefixes = {
    s: won.defaultContext["s"],
  };
  const minIsNum = isValidNumber(min);
  const maxIsNum = isValidNumber(max);
  const floorSizeVar = `${rootSubject}_floorSize.`;
  if (minIsNum || maxIsNum) {
    basicGraphPattern.push(
      `${rootSubject} s:floorSize/s:value ${floorSizeVar}.`
    );
  }
  if (minIsNum) {
    filterStrings.push(`FILTER (${floorSizeVar} >= ${min} )`);
  }
  if (maxIsNum) {
    filterStrings.push(`FILTER (${floorSizeVar} <= ${max} )`);
  }
  return wellFormedFilterReturn({ basicGraphPattern, filterStrings, prefixes });
}

export function filterNumOfRoomsRange(rootSubject, min, max) {
  const prefixes = {
    s: won.defaultContext["s"],
  };
  const basicGraphPattern = [];
  const filterStrings = [];
  const minIsNum = isValidNumber(min);
  const maxIsNum = isValidNumber(max);
  const numberOfRoomsVar = `${rootSubject}_numberOfRooms`;
  if (minIsNum || maxIsNum) {
    basicGraphPattern.push(
      `${rootSubject} s:numberOfRooms ${numberOfRoomsVar}.`
    );
  }
  if (minIsNum) {
    filterStrings.push(`FILTER (${numberOfRoomsVar} >= ${min} )`);
  }
  if (maxIsNum) {
    filterStrings.push(`FILTER (${numberOfRoomsVar} <= ${max} )`);
  }
  return wellFormedFilterReturn({ basicGraphPattern, filterStrings, prefixes });
}

export function filterRentRange(rootSubject, min, max, currency) {
  const prefixes = {
    s: won.defaultContext["s"],
  };
  let basicGraphPattern = [];
  const filterStrings = [];
  const minIsNum = isValidNumber(min);
  const maxIsNum = isValidNumber(max);
  const pricespecVar = `${rootSubject}_pricespec`;
  const currencyVar = `${rootSubject}_currency`;
  const priceVar = `${rootSubject}_price`;
  if ((minIsNum || maxIsNum) && currency) {
    filterStrings.push(`FILTER (${currencyVar} = "${currency}") `);
    basicGraphPattern = basicGraphPattern.concat([
      `${rootSubject} s:priceSpecification ${pricespecVar} .`,
      `${pricespecVar} s:price ${priceVar} .`,
      `${pricespecVar} s:priceCurrency ${currencyVar} .`,
    ]);
  }
  if (minIsNum) {
    filterStrings.push(`FILTER (${priceVar} >= ${min} )`);
  }
  if (maxIsNum) {
    filterStrings.push(`FILTER (${priceVar} <= ${max} )`);
  }

  return wellFormedFilterReturn({ basicGraphPattern, filterStrings, prefixes });
}

/**
 * @param {*} prefixes an object which' keys are the prefixes
 *  and values the long-form URIs.
 * @returns {String} in the form of e.g.
 * ```
 * prefix s: <http://schema.org/>
 * prefix won: <http://purl.org/webofneeds/model#>
 * ```
 */
export function prefixesString(prefixes) {
  if (!prefixes) {
    return "";
  } else {
    const prefixesStrings = Object.entries(prefixes).map(
      ([prefix, uri]) => `PREFIX ${prefix}: <${uri}>\n`
    );
    return prefixesStrings.join("");
  }
}
//TODO should return a context-def as well
