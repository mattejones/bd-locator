package xyz.mej.bdlocator.ingestion.cqc.dto;

/**
 * Retained for reference only — the CQC API returns address fields flat
 * on the provider/location response, not nested in an address object.
 * This class is not used in the ingestion pipeline.
 *
 * @deprecated use postalAddressLine1/postalAddressTownCity etc. directly
 */
@Deprecated
public class CqcAddress {}
