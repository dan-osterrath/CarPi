function decimal2Degrees(coordinate: number): string {
    const absolute = Math.abs(coordinate);
    const degrees = Math.floor(absolute);
    const minutesNotTruncated = (absolute - degrees) * 60;
    const minutes = Math.floor(minutesNotTruncated);
    const seconds = Math.floor((minutesNotTruncated - minutes) * 60);

    return `${degrees}°${minutes}'${seconds}"`;
}
function longitudeDecimal2Degrees(longitude: number): string {
    const degrees = decimal2Degrees(longitude);
    const direction = longitude >= 0 ? 'E' : 'W';
    return `${degrees}${direction}`;
}

function latitudeDecimal2Degrees(latitude: number): string {
    const degrees = decimal2Degrees(latitude);
    const direction = latitude >= 0 ? 'N' : 'S';
    return `${degrees}${direction}`;
}

function positionDecimal2Degrees(latitude: number, longitude: number): string {
    const lat = latitudeDecimal2Degrees(latitude);
    const lng = longitudeDecimal2Degrees(longitude);
    return `${lat}, ${lng}`;
}

export {
    latitudeDecimal2Degrees,
    longitudeDecimal2Degrees,
    positionDecimal2Degrees,
};