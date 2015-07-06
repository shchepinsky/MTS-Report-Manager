package loader;

/**
 * This is support class to return result from internal report parsing routines
 * marker field indicates current position in array
 * result indicates result of last operation
 */
class ParseResult {
    public Integer marker=0;
    public Boolean success=false;

    ParseResult(Integer marker, Boolean success) {
        this.marker = marker;
        this.success = success;
    }
}
