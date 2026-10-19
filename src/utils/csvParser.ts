import { PriceBar } from '../types/trading';

export interface CsvValidationResult {
  isValid: boolean;
  errors: string[];
  validBars: PriceBar[];
}

export function parseAndValidateCsv(csvContent: string): CsvValidationResult {
  const lines = csvContent.split('\n').map(l => l.trim()).filter(l => l.length > 0);
  const errors: string[] = [];

  if (lines.length < 2) {
    return {
      isValid: false,
      errors: ['The CSV file is empty or missing data rows.'],
      validBars: []
    };
  }

  const header = lines[0].split(',').map(c => c.trim().toLowerCase());
  const dateIdx = header.findIndex(c => c === 'date' || c === 'timestamp' || c === 'time');
  const openIdx = header.findIndex(c => c === 'open');
  const highIdx = header.findIndex(c => c === 'high');
  const lowIdx = header.findIndex(c => c === 'low');
  const closeIdx = header.findIndex(c => c === 'close' || c === 'adj close');
  const volumeIdx = header.findIndex(c => c === 'volume' || c === 'vol');

  if (dateIdx === -1) errors.push("Missing required 'Date' column in header.");
  if (openIdx === -1) errors.push("Missing required 'Open' column in header.");
  if (highIdx === -1) errors.push("Missing required 'High' column in header.");
  if (lowIdx === -1) errors.push("Missing required 'Low' column in header.");
  if (closeIdx === -1) errors.push("Missing required 'Close' column in header.");
  if (volumeIdx === -1) errors.push("Missing required 'Volume' column in header.");

  if (errors.length > 0) {
    return { isValid: false, errors, validBars: [] };
  }

  const validBars: PriceBar[] = [];
  const seenDates = new Set<string>();

  for (let i = 1; i < lines.length; i++) {
    const lineNum = i + 1;
    const cols = lines[i].split(',').map(c => c.trim());
    const minCols = Math.max(dateIdx, openIdx, highIdx, lowIdx, closeIdx, volumeIdx) + 1;

    if (cols.length < minCols) {
      if (errors.length < 10) errors.push(`Line ${lineNum}: Missing column values.`);
      continue;
    }

    const rawDate = cols[dateIdx];
    const parsedDate = new Date(rawDate);
    if (isNaN(parsedDate.getTime())) {
      if (errors.length < 10) errors.push(`Line ${lineNum}: Invalid date format '${rawDate}'.`);
      continue;
    }

    const isoDate = parsedDate.toISOString().split('T')[0];
    if (seenDates.has(isoDate)) {
      if (errors.length < 10) errors.push(`Line ${lineNum}: Duplicate date '${isoDate}'.`);
      continue;
    }

    const open = parseFloat(cols[openIdx]);
    const high = parseFloat(cols[highIdx]);
    const low = parseFloat(cols[lowIdx]);
    const close = parseFloat(cols[closeIdx]);
    const volume = parseInt(cols[volumeIdx], 10);

    if (isNaN(open) || isNaN(high) || isNaN(low) || isNaN(close) || isNaN(volume)) {
      if (errors.length < 10) errors.push(`Line ${lineNum}: Non-numeric OHLCV value.`);
      continue;
    }

    if (open <= 0 || high <= 0 || low <= 0 || close <= 0 || volume < 0) {
      if (errors.length < 10) errors.push(`Line ${lineNum}: Prices must be strictly positive and volume non-negative.`);
      continue;
    }

    if (high < low) {
      if (errors.length < 10) errors.push(`Line ${lineNum}: Invalid OHLC - High (${high}) is less than Low (${low}).`);
      continue;
    }
    if (high < open || high < close) {
      if (errors.length < 10) errors.push(`Line ${lineNum}: Invalid OHLC - High must be >= Open and Close.`);
      continue;
    }
    if (low > open || low > close) {
      if (errors.length < 10) errors.push(`Line ${lineNum}: Invalid OHLC - Low must be <= Open and Close.`);
      continue;
    }

    seenDates.add(isoDate);
    validBars.push({
      date: isoDate,
      open,
      high,
      low,
      close,
      volume
    });
  }

  validBars.sort((a, b) => a.date.localeCompare(b.date));

  return {
    isValid: errors.length === 0 && validBars.length > 0,
    errors,
    validBars
  };
}
