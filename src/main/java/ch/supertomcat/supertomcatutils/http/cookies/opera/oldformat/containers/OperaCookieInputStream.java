package ch.supertomcat.supertomcatutils.http.cookies.opera.oldformat.containers;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.supertomcatutils.http.cookies.opera.oldformat.containers.values.OperaByteValue;
import ch.supertomcat.supertomcatutils.http.cookies.opera.oldformat.containers.values.OperaIntegerValue;
import ch.supertomcat.supertomcatutils.http.cookies.opera.oldformat.containers.values.OperaLongValue;
import ch.supertomcat.supertomcatutils.http.cookies.opera.oldformat.containers.values.OperaShortValue;
import ch.supertomcat.supertomcatutils.http.cookies.opera.oldformat.containers.values.OperaStringValue;
import ch.supertomcat.supertomcatutils.http.cookies.opera.oldformat.containers.values.OperaTimeValue;

/**
 * OperaCookieInputStream
 */
public class OperaCookieInputStream extends DataInputStream {
	/*
	 * Format Description:
	 * (Original from: http://www.opera.com/docs/fileformats/)
	 * 
	 * # Data Types: #
	 * Integers are unsigned stored in Big-Endian (Most Significant Byte first).
	 * Integers in records are stored in Big-Endian (Most Significant Byte first), but can be signed, unsigned or truncated (zero bytes removed).
	 * 
	 * Following data types are used:
	 * int* -> Signed Integer of * bits
	 * uint* -> Unsigned Integer of * bits
	 * byte -> 8 bit unsigned value
	 * String -> Sequence of characters (not null terminated)
	 * time_t -> uint32 timestamp value in seconds since 00:00 Jan 1, 1970 GMT
	 * tag_id_type -> Unsigned integer, whose size is defined by idtag_length field in the header.
	 * payload_length_type -> Unsigned integer, whose size is defined by the length_length field in the header.
	 * record -> see below
	 * 
	 * # Header: #
	 * 
	 * uint32 file_version_number;
	 * 
	 * uint32 app_version_number;
	 * 
	 * // number of bytes in the id tag (1 = uint8, 2 = uint16, 3 = uint24, 4 = unit32), presently 1
	 * uint16 idtag_length;
	 * 
	 * // number of bytes in the length part of a record (1 = uint8, 2 = uint16, 3 = uint24, 4 = unit32), presently 2
	 * uint16 length_length;
	 * 
	 * // array of records, number determined by length of file
	 * struct record items[];
	 * 
	 * ## file_version_number: ##
	 * The present version number of the file format (file_version_number) is 0x00001000, where the lower 12 bits (bitmask 0x00000fff) represent the minor
	 * version number, the rest is the major version number. Changes in the minor version must not be used if the file format is changed in such a manner that
	 * older versions of the software cannot read the file successfully. If the major version number is newer (or older) than the application can read, it must
	 * not read the file.
	 * 
	 * The integer sizes are absolute for a given major version, and the integer size for the file version number is fixed in any version.
	 * 
	 * ## app_version_number: ##
	 * The "app_version_number" is the version number of the application and is independent of the file_version_number. It may be used by the application to
	 * determine necessary actions needed to provide forward or backward compatibility that is outside the scope of the file formats. The interpretation of the
	 * application version number is application dependent.
	 * 
	 * ## idtag_length / length_length: ##
	 * The "idtag_length" and "length_length" fields gives the number of bytes used in the records for the idtags, as defined by the tag_id_type, and the
	 * payload length fields, as defined by payload_length_type, respectively.
	 * 
	 * After the header, only records follow. The organization of the records and their interpretation is application specific.
	 * 
	 * # Forward Compatibility #
	 * 
	 * An older version of an application using this file format that is NOT able to use long integers should, regardless of this, try to process the file, but
	 * should bypass the record if the tag of the record's numerical value exceeds the version's own integer range, i.e. the integer overflows. However, if the
	 * the length of the record exceeds the application's limits on integers or buffer capabilities, it must not continue to process the file.
	 * 
	 * All applications must ignore tag values that they do not understand.
	 * 
	 * # Data Records: #
	 * 
	 * struct record
	 * {
	 * // application specific tag to identify content type
	 * tag_id_type tag_id;
	 * 
	 * // length of payload
	 * payload_length_type length;
	 * 
	 * // Payload/content of the record
	 * bytepayload[length];
	 * };
	 * 
	 * ## tag_id: ##
	 * The identifier of the record. This value is application specific, and can be used to indicate the meaning of the payload content.
	 * 
	 * The actual content type of the record depends on the definitions used for the actual file or super-record.
	 * 
	 * Tag_id values in which the MSB (Most Significant Bit) is set to 1, are reserved for records with implicit no length.
	 * Such tag_id fields are NOT followed by a length field, nor a payload buffer. Such records are used as Boolean flags: True if present, False if not
	 * present.
	 * 
	 * In the binary storage of a file this means that the MSB of the internal storage integer must be stored as the MSB of the first byte in the tag field.
	 * This places a limit on how many tags can be used for a given tag_id integer length. When a file is read into a program, the program must take care to
	 * move the MSB of the binary stored tag to a common (internal) bit position, such as the MSB of the program's own unsigned integers.
	 * 
	 * bytes Max id available (excluding MSB)
	 * 1 0x7f
	 * 2 0x7fff
	 * 3 0x7fffff
	 * 4 0x7fffffff
	 * 
	 * ## length: ##
	 * This field is the number of bytes in the payload that immediately follow the field. It may be zero.
	 * 
	 * ## payload: ##
	 * The payload is a sequence of bytes of the length indicated by the length field.
	 * 
	 * The meaning of the contents is indicated by the definition for the given record or file structure. Examples of organization may be an array of records,
	 * unsigned integers, signed integers, or characters.
	 * 
	 * Single item integers (signed or unsigned) may be truncated (zero bytes removed), but arrays of integers must always use a fixed number of bytes to
	 * represent values and derive the number of items from the payload length. If the number of bytes needed to represent the values changes in a future
	 * version a new tag should be used.
	 * 
	 * # Cookie File format #
	 * 
	 * The cookie file is organized as a tree of domain name components, each component then holds a tree of path components and each path component may contain
	 * a number of cookies.
	 * 
	 * NOTE: The components are a sequence of records, teminated with a flag record, not a single record.
	 * 
	 * ## Structure ##
	 * 
	 * ### Domain Components ###
	 * 
	 * The domain components are used to organize the cookies for each server and domain for which cookies or cookie filtering capabilities are defined.
	 * 
	 * A domain component is started with a domain record, which holds the domain name and some flags for that particular domain. It is then followed by a path
	 * component holding the cookies and subdirectory path components (and cookies), followed with a path component terminator and any number of subdomain
	 * components before it is terminated by a domain-end flag record.
	 * 
	 * All names of domain components are non-dotted, except IP addresses, which can only be stored with the complete IP address as a Quad dotted string, e.g.
	 * "10.11.12.13", are stored at the top level, and cannot contain any subdomains.
	 * 
	 * E.g: cookies for the domain www.opera.com will be stored as:
	 * ["com" record]
	 * ["opera" record]
	 * ["www" record]
	 * [cookies]
	 * [Path components]
	 * [Path component terminator]
	 * [other domains]
	 * [end of domain flag ("www")]
	 * [end of domain flag ("opera")]
	 * [end of domain flag ("com")]
	 * 
	 * A Domain Record uses the tag "0x01" and contains a sequence of these fields:
	 * * 0x001E -> string -> The name of the domain part
	 * 
	 * * 0x001F -> int8 -> How cookies are filtered for this domain. If not present, the filtering of the parent domain is used.
	 * 1 = All cookies from this domain are accepted.
	 * 2 = No cookies from this domain are accepted.
	 * 3 = All cookies from this server are accepted. Overrides 1 and 2 for higher level domains automatics.
	 * 4 = No cookies from this server are accepted. Overrides 1 and 2 for higher level domains.
	 * Domain settings apply to all subdomains, except those with a server specific selection.
	 * 
	 * * 0x0021 -> int8 -> Handling of cookies that have explicit paths which do not match the URL setting the cookies. If enabled in the privacy preferences
	 * the default is to warn the user, but when warning is enabled such cookies can be filtered by their domains: Value 1 indicates reject, and 2 is accept
	 * automatically.
	 * 
	 * * 0x0025 -> int8 -> While in the "Warn about third party cookies" mode, this field can be used to automatically filter such cookies.
	 * 1 = All third party cookies from this domain are accepted.
	 * 2 = No third party cookies from this domain are accepted.
	 * 3 = All third party cookies from this server are accepted. Overrides 1 and 2 for higher level domains automatics.
	 * 4 = No third party cookies from this server are accepted. Overrides 1 and 2 for higher level domains.
	 * Domain settings apply to all subdomains, except those with a server specific selection.
	 * 
	 * This record can be followed by zero or more path components defining toplevel paths on servers in the domain and always terminated by a path component
	 * terminator record. Then zero or more domain components may follow.
	 * 
	 * A domain component is terminated by a (0x0004 | MSB_VALUE) flag record.
	 * 
	 * ### Path Components ###
	 * 
	 * The path components organize the cookies defined for a given directory in a given domain, as well any subdirectories of this directory that have cookies
	 * defined.
	 * 
	 * Except for the path component starting immediately after the domain component record, each path component always starts with a path record, and is then
	 * followed by any number of cookie records and subdirectory path components.
	 * 
	 * The path record uses the record id "0x0002" and the record has this field record:
	 * * 0x001D -> string -> The name of the path part
	 * 
	 * The path component terminator is the (0x0005 | MSB_VALUE) flag record.
	 * 
	 * ### Cookie Records ###
	 * 
	 * NOTE: "(0x0001 | MSB_VALUE)" means that the most significant bit in the local unsigned integer is to be set. If 32 bit values are used, that means the
	 * tag's value is 0x80000001.
	 * 
	 * The cookie entries are stored in records of type "0x0003" and have the following field records:
	 * * 0x0010 -> string -> The name of the cookie
	 * 
	 * * 0x0011 -> string -> The value of the cookie
	 * 
	 * * 0x0012 -> time_t -> Expiry date
	 * 
	 * * 0x0013 -> time_t -> Last used
	 * 
	 * * 0x0014 -> string -> Comment/Description of use (RFC 2965)
	 * 
	 * * 0x0015 -> string -> URL for Comment/Description of use (RFC 2965)
	 * 
	 * * 0x0016 -> string -> The domain received with version=1 cookies (RFC 2965)
	 * 
	 * * 0x0017 -> string -> The path received with version=1 cookies (RFC 2965)
	 * 
	 * * 0x0018 -> string -> The port limitations received with version=1 cookies (RFC 2965)
	 * 
	 * * (0x0019 | MSB_VALUE) -> flag -> The cookie will only be sent to HTTPS servers.
	 * 
	 * * 0x001A -> int8+ -> Version number of cookie (RFC 2965)
	 * 
	 * * (0x001B | MSB_VALUE) -> flag -> This cookie will only be sent to the server that sent it.
	 * 
	 * * (0x001C | MSB_VALUE) -> flag -> Reserved for delete protection: Not yet implemented
	 * 
	 * * (0x0020 | MSB_VALUE) -> flag -> This cookie will not be sent if the path is only a prefix of the URL. If the path is /foo, /foo/bar will match but not
	 * /foobar.
	 * 
	 * * (0x0022 | MSB_VALUE) -> flag -> If true, this cookie was set as the result of a password login form, or by a URL that was retrieved using a cookie that
	 * can be tracked back to such a cookie.
	 * 
	 * * (0x0023 | MSB_VALUE) -> flag -> If true, this cookie was set as the result of a HTTP authentication login, or by a URL that was retrieved using a
	 * cookie
	 * that can be tracked back to such a cookie.
	 * 
	 * * (0x0024 | MSB_VALUE) -> flag -> In "Display Third party cookies" mode this flag will be set if the cookie was set by a third party server, and only
	 * these cookies will be sent if the URL is a third party. Cookies that were received when loading a URL from the server directly will not be sent to third
	 * party URLs in this mode. The reverse is NOT true.
	 * NOTE: If a third party server redirects back to the first party server, the redirected URL is considered third party.
	 */

	/**
	 * MSB Value for 1 Byte ID Tags
	 */
	public static final int O_MSB_VALUE_1 = 0x80;

	/**
	 * MSB Value for 2 Byte ID Tags
	 */
	public static final int O_MSB_VALUE_2 = 0x8000;

	/**
	 * MSB Value for 3 Byte ID Tags
	 */
	public static final int O_MSB_VALUE_3 = 0x800000;

	/**
	 * MSB Value for 3 Byte ID Tags
	 */
	public static final long O_MSB_VALUE_L = 0x800000L;

	/**
	 * MSB Value for 4 Byte ID Tags
	 */
	public static final int O_MSB_VALUE_4 = 0x80000000;

	/**
	 * Null byte
	 */
	public static final int O_NULL_BYTE = 0x00;

	/**
	 * Domain Start 0x0001
	 */
	public static final int O_DOMAIN_START = 0x01;

	/**
	 * Domain End (0x0004 | MSB_VALUE)
	 */
	public static final int O_DOMAIN_END_1 = 0x04 | O_MSB_VALUE_1;

	/**
	 * Domain End (0x0004 | MSB_VALUE)
	 */
	public static final int O_DOMAIN_END_2 = 0x0004 | O_MSB_VALUE_2;

	/**
	 * Domain End (0x0004 | MSB_VALUE)
	 */
	public static final int O_DOMAIN_END_3 = 0x000004 | O_MSB_VALUE_3;

	/**
	 * Domain End (0x0004 | MSB_VALUE)
	 */
	public static final long O_DOMAIN_END_L = 0x000004L | O_MSB_VALUE_L;

	/**
	 * Domain End (0x0004 | MSB_VALUE)
	 */
	public static final int O_DOMAIN_END_4 = 0x00000004 | O_MSB_VALUE_4;

	/**
	 * String
	 */
	public static final int O_DOMAIN_NAME = 0x1E;

	/**
	 * Numeric
	 * 1 = Alle von Domain akzeptieren
	 * 2 = Alle von Domain ablehnen
	 * 3 = Alle von Server akzeptieren
	 * 4 = Alle von Server ablehnen
	 */
	public static final int O_DOMAIN_ACCEPT = 0x1F;

	/**
	 * Numeric
	 * 1 = Ablehnen
	 * 2 = Automatisch akzeptieren
	 */
	public static final int O_DOMAIN_PATH_NOT_MATCHING = 0x21;

	/**
	 * Numeric
	 * 1 = Alle von Domain akzeptieren
	 * 2 = Alle von Domain ablehnen
	 * 3 = Alle von Server akzeptieren
	 * 4 = Alle von Server ablehnen
	 */
	public static final int O_DOMAIN_THIRD_PARTY_COOKIE = 0x25;

	/**
	 * Record
	 */
	public static final int O_PATH_START = 0x02;

	/**
	 * String
	 */
	public static final int O_PATH_NAME = 0x1D;

	/**
	 * Path End (0x0005 | MSB_VALUE)
	 */
	public static final int O_PATH_END_1 = 0x05 | O_MSB_VALUE_1;

	/**
	 * Path End (0x0005 | MSB_VALUE)
	 */
	public static final int O_PATH_END_2 = 0x0005 | O_MSB_VALUE_2;

	/**
	 * Path End (0x0005 | MSB_VALUE)
	 */
	public static final int O_PATH_END_3 = 0x000005 | O_MSB_VALUE_3;

	/**
	 * Path End (0x0005 | MSB_VALUE)
	 */
	public static final int O_PATH_END_4 = 0x00000005 | O_MSB_VALUE_4;

	/**
	 * Record
	 */
	public static final int O_COOKIE_START = 0x03;

	/**
	 * String
	 */
	public static final int O_COOKIE_NAME = 0x10;

	/**
	 * String
	 */
	public static final int O_COOKIE_VALUE = 0x11;

	/**
	 * Time
	 */
	public static final int O_COOKIE_EXPIRES = 0x12;

	/**
	 * Time
	 */
	public static final int O_COOKIE_LAST_USED = 0x13;

	/**
	 * String
	 */
	public static final int O_COOKIE_COMMENT = 0x14;

	/**
	 * String
	 */
	public static final int O_COOKIE_COMMENT_URL = 0x15;

	/**
	 * String
	 */
	public static final int O_COOKIE_RECEIVED_DOMAIN = 0x16;

	/**
	 * String
	 */
	public static final int O_COOKIE_RECEIVED_PATH = 0x17;

	/**
	 * String
	 */
	public static final int O_COOKIE_PORTLIST = 0x18;

	/**
	 * true wenn vorhanden
	 */
	public static final int O_COOKIE_SECURE_1 = 0x19 | O_MSB_VALUE_1;

	/**
	 * true wenn vorhanden
	 */
	public static final int O_COOKIE_SECURE_2 = 0x0019 | O_MSB_VALUE_2;

	/**
	 * true wenn vorhanden
	 */
	public static final int O_COOKIE_SECURE_3 = 0x000019 | O_MSB_VALUE_3;

	/**
	 * true wenn vorhanden
	 */
	public static final int O_COOKIE_SECURE_4 = 0x00000019 | O_MSB_VALUE_4;

	/**
	 * unsigned numerical
	 */
	public static final int O_COOKIE_VERSION = 0x1A;

	/**
	 * true wenn vorhanden
	 */
	public static final int O_COOKIE_SERVER_ONLY_1 = 0x1B | O_MSB_VALUE_1;

	/**
	 * true wenn vorhanden
	 */
	public static final int O_COOKIE_SERVER_ONLY_2 = 0x001B | O_MSB_VALUE_2;

	/**
	 * true wenn vorhanden
	 */
	public static final int O_COOKIE_SERVER_ONLY_3 = 0x00001B | O_MSB_VALUE_3;

	/**
	 * true wenn vorhanden
	 */
	public static final int O_COOKIE_SERVER_ONLY_4 = 0x0000001B | O_MSB_VALUE_4;

	/**
	 * true wenn vorhanden
	 */
	public static final int O_COOKIE_PROTECTED_1 = 0x1C | O_MSB_VALUE_1;

	/**
	 * true wenn vorhanden
	 */
	public static final int O_COOKIE_PROTECTED_2 = 0x001C | O_MSB_VALUE_2;

	/**
	 * true wenn vorhanden
	 */
	public static final int O_COOKIE_PROTECTED_3 = 0x00001C | O_MSB_VALUE_3;

	/**
	 * true wenn vorhanden
	 */
	public static final int O_COOKIE_PROTECTED_4 = 0x0000001C | O_MSB_VALUE_4;

	/**
	 * true wenn vorhanden
	 */
	public static final int O_COOKIE_PATH_PREFIX_1 = 0x20 | O_MSB_VALUE_1;

	/**
	 * true wenn vorhanden
	 */
	public static final int O_COOKIE_PATH_PREFIX_2 = 0x0020 | O_MSB_VALUE_2;

	/**
	 * true wenn vorhanden
	 */
	public static final int O_COOKIE_PATH_PREFIX_3 = 0x000020 | O_MSB_VALUE_3;

	/**
	 * true wenn vorhanden
	 */
	public static final int O_COOKIE_PATH_PREFIX_4 = 0x00000020 | O_MSB_VALUE_4;

	/**
	 * true wenn vorhanden
	 */
	public static final int O_COOKIE_PASSWORD_1 = 0x22 | O_MSB_VALUE_1;

	/**
	 * true wenn vorhanden
	 */
	public static final int O_COOKIE_PASSWORD_2 = 0x0022 | O_MSB_VALUE_2;

	/**
	 * true wenn vorhanden
	 */
	public static final int O_COOKIE_PASSWORD_3 = 0x000022 | O_MSB_VALUE_3;

	/**
	 * true wenn vorhanden
	 */
	public static final int O_COOKIE_PASSWORD_4 = 0x00000022 | O_MSB_VALUE_4;

	/**
	 * true wenn vorhanden
	 */
	public static final int O_COOKIE_AUTHENTICATE_1 = 0x23 | O_MSB_VALUE_1;

	/**
	 * true wenn vorhanden
	 */
	public static final int O_COOKIE_AUTHENTICATE_2 = 0x0023 | O_MSB_VALUE_2;

	/**
	 * true wenn vorhanden
	 */
	public static final int O_COOKIE_AUTHENTICATE_3 = 0x000023 | O_MSB_VALUE_3;

	/**
	 * true wenn vorhanden
	 */
	public static final int O_COOKIE_AUTHENTICATE_4 = 0x00000023 | O_MSB_VALUE_4;

	/**
	 * true wenn vorhanden
	 */
	public static final int O_COOKIE_THIRD_PARTY_1 = 0x24 | O_MSB_VALUE_1;

	/**
	 * true wenn vorhanden
	 */
	public static final int O_COOKIE_THIRD_PARTY_2 = 0x0024 | O_MSB_VALUE_2;

	/**
	 * true wenn vorhanden
	 */
	public static final int O_COOKIE_THIRD_PARTY_3 = 0x000024 | O_MSB_VALUE_3;

	/**
	 * true wenn vorhanden
	 */
	public static final int O_COOKIE_THIRD_PARTY_4 = 0x00000024 | O_MSB_VALUE_4;

	/**
	 * Logger for this class
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Constructor
	 * 
	 * @param in InputStream
	 */
	public OperaCookieInputStream(InputStream in) {
		super(in);
	}

	/**
	 * Read unsigned int consisting of 4 bytes
	 * 
	 * @return unsigned int returned as a long, because Java has no unsigned int
	 * @throws IOException
	 */
	public long read3ByteUnsignedInt() throws IOException {
		byte b1 = readByte();
		byte b2 = readByte();
		byte b3 = readByte();
		int signedInt = ((b1 & 0xFF) << 16) | ((b2 & 0xFF) << 8) | (b3 & 0xFF);
		return Integer.toUnsignedLong(signedInt);
	}

	/**
	 * Read unsigned int consisting of 4 bytes as int. Big values of Integer.MAX_VALUE will overflow, so this needs to be handled by the caller.
	 * 
	 * @return unsinged int
	 * @throws IOException
	 */
	public int read3ByteUnsignedIntAsInt() throws IOException {
		byte b1 = readByte();
		byte b2 = readByte();
		byte b3 = readByte();
		return ((b1 & 0xFF) << 16) | ((b2 & 0xFF) << 8) | (b3 & 0xFF);
	}

	/**
	 * Read unsigned int consisting of 4 bytes
	 * 
	 * @return unsigned int returned as a long, because Java has no unsigned int
	 * @throws IOException
	 */
	public long readUnsignedInt() throws IOException {
		int signedInt = readInt();
		return Integer.toUnsignedLong(signedInt);
	}

	/**
	 * Read unsigned int consisting of 4 bytes as int. Big values of Integer.MAX_VALUE will overflow, so this needs to be handled by the caller.
	 * 
	 * @return unsinged int
	 * @throws IOException
	 */
	public int readUnsignedIntAsInt() throws IOException {
		return readInt();
	}

	/**
	 * Read String
	 * 
	 * @param value Value Container
	 * @param recordSize Record Size
	 * @return Bytes Read
	 * @throws IOException
	 */
	public long readStringValue(OperaStringValue value, int recordSize) throws IOException {
		long payloadSize = readPayloadSize(recordSize);

		StringBuilder sb = new StringBuilder();
		for (long i = 0; i < payloadSize; i++) {
			sb.append((char)readByte());
		}
		value.setValue(sb.toString());

		return recordSize + payloadSize;
	}

	/**
	 * Read Time
	 * 
	 * @param value Value Container
	 * @param recordSize Record Size
	 * @return Bytes Read
	 * @throws IOException
	 */
	public long readTimeValue(OperaTimeValue value, int recordSize) throws IOException {
		long payloadSize = readPayloadSize(recordSize);

		if (payloadSize == 4) {
			value.setValue(readUnsignedInt());
		} else if (payloadSize == 8) {
			/*
			 * Unlike specified in the format description it seems like they store uint64 and not uint32.
			 * But the first 4 bytes are filled with zeros.
			 */
			readUnsignedInt();
			value.setValue(readUnsignedInt());
		} else {
			throw new IOException("Unexpected Payload Size for time_t value: " + payloadSize);
		}

		return recordSize + payloadSize;
	}

	/**
	 * Read Int8
	 * 
	 * @param value Value Container
	 * @param recordSize Record Size
	 * @return Bytes Read
	 * @throws IOException
	 */
	public long readInt8Value(OperaByteValue value, int recordSize) throws IOException {
		long payloadSize = readPayloadSize(recordSize);

		if (payloadSize != 1) {
			throw new IOException("Unexpected Payload Size for int8 value: " + payloadSize);
		}
		value.setValue(readByte());

		return recordSize + payloadSize;
	}

	/**
	 * Read Int16
	 * 
	 * @param value Value Container
	 * @param recordSize Record Size
	 * @return Bytes Read
	 * @throws IOException
	 */
	public long readInt16Value(OperaShortValue value, int recordSize) throws IOException {
		long payloadSize = readPayloadSize(recordSize);

		if (payloadSize != 2) {
			throw new IOException("Unexpected Payload Size for int16 value: " + payloadSize);
		}
		value.setValue(readShort());

		return recordSize + payloadSize;
	}

	/**
	 * Read Int32
	 * 
	 * @param value Value Container
	 * @param recordSize Record Size
	 * @return Bytes Read
	 * @throws IOException
	 */
	public long readInt32Value(OperaIntegerValue value, int recordSize) throws IOException {
		long payloadSize = readPayloadSize(recordSize);

		if (payloadSize != 4) {
			throw new IOException("Unexpected Payload Size for int32 value: " + payloadSize);
		}
		value.setValue(readInt());

		return recordSize + payloadSize;
	}

	/**
	 * Read Int8
	 * 
	 * @param value Value Container
	 * @param recordSize Record Size
	 * @return Bytes Read
	 * @throws IOException
	 */
	public long readUInt8Value(OperaIntegerValue value, int recordSize) throws IOException {
		long payloadSize = readPayloadSize(recordSize);

		if (payloadSize != 1) {
			throw new IOException("Unexpected Payload Size for uint8 value: " + payloadSize);
		}
		value.setValue(readUnsignedByte());

		return recordSize + payloadSize;
	}

	/**
	 * Read Int16
	 * 
	 * @param value Value Container
	 * @param recordSize Record Size
	 * @return Bytes Read
	 * @throws IOException
	 */
	public long readUInt16Value(OperaIntegerValue value, int recordSize) throws IOException {
		long payloadSize = readPayloadSize(recordSize);

		if (payloadSize != 2) {
			throw new IOException("Unexpected Payload Size for uint16 value: " + payloadSize);
		}
		value.setValue(readUnsignedShort());

		return recordSize + payloadSize;
	}

	/**
	 * Read Int32
	 * 
	 * @param value Value Container
	 * @param recordSize Record Size
	 * @return Bytes Read
	 * @throws IOException
	 */
	public long readUInt32Value(OperaLongValue value, int recordSize) throws IOException {
		long payloadSize = readPayloadSize(recordSize);

		if (payloadSize != 4) {
			throw new IOException("Unexpected Payload Size for uint32 value: " + payloadSize);
		}
		value.setValue(readUnsignedInt());

		return recordSize + payloadSize;
	}

	/**
	 * Read header
	 * 
	 * @param och OperaCookieHeader
	 * @return Bytes read
	 * @throws IOException
	 */
	public int read(OperaCookieHeader och) throws IOException {
		int read = 0;
		read += readFileVersion(och);
		read += readAppVersion(och);
		read += readTagSize(och);
		read += readRecordSize(och);
		return read;
	}

	private int readFileVersion(OperaCookieHeader och) throws IOException {
		long fileVersion = readUnsignedInt();
		och.setFileVersion(fileVersion);
		return 4;
	}

	private int readAppVersion(OperaCookieHeader och) throws IOException {
		long appVersion = readUnsignedInt();
		och.setAppVersion(appVersion);
		return 4;
	}

	private int readTagSize(OperaCookieHeader och) throws IOException {
		int tagSize = readUnsignedShort();
		och.setTagSize(tagSize);
		return 2;
	}

	private int readRecordSize(OperaCookieHeader och) throws IOException {
		int recordSize = readUnsignedShort();
		och.setRecordSize(recordSize);
		return 2;
	}

	/**
	 * Read Tag ID
	 * 
	 * @param tagSize Tag Size (1 = uint8, 2 = uint16, 3 = uint24, 4 = uint32)
	 * @return Tag ID or -1 if EOF
	 * @throws IOException
	 */
	public int readTagID(int tagSize) throws IOException {
		try {
			switch (tagSize) {
				case 1:
					return readUnsignedByte();
				case 2:
					return readUnsignedShort();
				case 3:
					/*
					 * Theoretically we would need to return a long (using read3ByteUnsignedInt), but then switch case could not be used to check Tag IDs.
					 * So to fix this problem, we just let the integer overflow and also define the constant, so that the integer overflows.
					 */
					return read3ByteUnsignedIntAsInt();
				case 4:
					/*
					 * Theoretically we would need to return a long (using readUnsignedInt), but then switch case could not be used to check Tag IDs.
					 * So to fix this problem, we just let the integer overflow and also define the constant, so that the integer overflows.
					 */
					return readUnsignedIntAsInt();
				default:
					throw new IOException("Could not read tag ID, not supported tag size: " + tagSize);
			}
		} catch (EOFException e) {
			return -1;
		}
	}

	/**
	 * Read Payload Size
	 * 
	 * @param recordSize Record Size (1 = uint8, 2 = uint16, 3 = uint24, 4 = uint32)
	 * @return Payload Size
	 * @throws IOException
	 */
	public long readPayloadSize(int recordSize) throws IOException {
		switch (recordSize) {
			case 1:
				return readUnsignedByte();
			case 2:
				return readUnsignedShort();
			case 4:
				return readUnsignedInt();
			default:
				throw new IOException("Could not read payload size, not supported record size: " + recordSize);
		}
	}

	/**
	 * Read domain components
	 * 
	 * @param tagSize Tag Size
	 * @param recordSize Record Size
	 * @return List of domain components
	 * @throws IOException
	 */
	public List<OperaDomain> readDomainComponents(int tagSize, int recordSize) throws IOException {
		// Country Top Level Domains (like "ch", "de", ...)
		List<OperaDomain> countryTopLevelDomains = new ArrayList<>();

		Stack<OperaDomain> domainStack = new Stack<>();
		Stack<OperaPath> pathStack = new Stack<>();

		int tagID;
		while ((tagID = readTagID(tagSize)) != -1) {
			switch (tagID) {
				case O_DOMAIN_START:
					OperaDomain currentDomain = new OperaDomain();
					OperaDomain parentDomain;
					if (domainStack.isEmpty()) {
						parentDomain = null;
						countryTopLevelDomains.add(currentDomain);
					} else {
						parentDomain = domainStack.peek();
						parentDomain.addSubDomain(currentDomain);
					}
					readDomainRecord(currentDomain, parentDomain, tagSize, recordSize);
					domainStack.push(currentDomain);
					break;
				case O_DOMAIN_END_1:
				case O_DOMAIN_END_2:
				case O_DOMAIN_END_3:
				case O_DOMAIN_END_4:
					if (domainStack.isEmpty()) {
						logger.error("There are no domains on the stack, even though there should be one");
					} else {
						domainStack.pop();
					}

					if (!pathStack.isEmpty()) {
						logger.error("There are paths on the stack, which were never closed: {}", pathStack.size());
					}
					pathStack.clear();
					break;
				case O_PATH_START:
					OperaPath currentPath = new OperaPath();
					OperaPath parentPath;
					OperaDomain parentDomainForPath = domainStack.peek();
					if (pathStack.isEmpty()) {
						parentPath = null;
						parentDomainForPath.addPath(currentPath);
					} else {
						parentPath = pathStack.peek();
						parentPath.addSubPath(currentPath);
					}
					readPathRecord(currentPath, parentPath, tagSize, recordSize);
					pathStack.push(currentPath);
					break;
				case O_PATH_END_1:
				case O_PATH_END_2:
				case O_PATH_END_3:
				case O_PATH_END_4:
					/*
					 * Appears also after domain record with root domain like "com", even though no O_PATH_START was read
					 */
					if (!pathStack.isEmpty()) {
						pathStack.pop();
					}
					break;
				case O_COOKIE_START:
					OperaDomain parentDomainForCookie = domainStack.peek();
					OperaPath parentPathForCookie = null;
					if (!pathStack.isEmpty()) {
						parentPathForCookie = pathStack.peek();
					}

					OperaCookie currentCookie = new OperaCookie();
					currentCookie.setDomain(parentDomainForCookie.getFullyQualifiedName());
					if (parentPathForCookie != null) {
						parentPathForCookie.addCookie(currentCookie);
						currentCookie.setPath(parentPathForCookie.getFullyQualifiedName());
					} else {
						parentDomainForCookie.addCookie(currentCookie);
						currentCookie.setPath("");
					}

					readCookieRecord(currentCookie, tagSize, recordSize);
					break;
				default:
					logger.error("Unexpected Tag ID: {}", tagID);
					break;
			}
		}

		return countryTopLevelDomains;
	}

	/**
	 * Read domain record
	 * 
	 * @param operaDomain Domain
	 * @param parentDomain Parent Domain or null if no parent
	 * @param tagSize Tag Size
	 * @param recordSize Record Size
	 * @return Bytes Read
	 * @throws IOException
	 */
	public long readDomainRecord(OperaDomain operaDomain, OperaDomain parentDomain, int tagSize, int recordSize) throws IOException {
		long payloadSize = readPayloadSize(recordSize);

		long read = 0;
		while (read < payloadSize) {
			int tagID = readTagID(tagSize);
			read += tagSize;

			if (tagID == -1) {
				throw new IOException("Unexpected end of file");
			}

			switch (tagID) {
				case O_DOMAIN_NAME:
					OperaStringValue strName = new OperaStringValue();
					read += readStringValue(strName, recordSize);
					String name = strName.getValue();
					operaDomain.setName(name);
					if (parentDomain != null) {
						operaDomain.setFullyQualifiedName(name + "." + parentDomain.getFullyQualifiedName());
					} else {
						operaDomain.setFullyQualifiedName(name);
					}
					break;
				case O_DOMAIN_ACCEPT:
					OperaByteValue acceptValue = new OperaByteValue();
					read += readInt8Value(acceptValue, recordSize);
					operaDomain.setAccept(acceptValue.getValue());
					break;
				case O_DOMAIN_PATH_NOT_MATCHING:
					OperaByteValue notMatchValue = new OperaByteValue();
					read += readInt8Value(notMatchValue, recordSize);
					operaDomain.setNotMatch(notMatchValue.getValue());
					break;
				case O_DOMAIN_THIRD_PARTY_COOKIE:
					OperaByteValue thirdPartyValue = new OperaByteValue();
					read += readInt8Value(thirdPartyValue, recordSize);
					operaDomain.setThirdParty(thirdPartyValue.getValue());
					break;
				case O_NULL_BYTE:
					break;
				default:
					logger.error("Unexpected Tag ID in Domain Record: {}", tagID);
					break;
			}
		}

		if (read != payloadSize) {
			throw new IOException("Read too less or too much bytes. Payload Size: " + payloadSize + ", Read: " + read);
		}
		return read;
	}

	/**
	 * Read path record
	 * 
	 * @param operaPath Path
	 * @param parentPath Parent Path or null if no parent
	 * @param tagSize Tag Size
	 * @param recordSize Record Size
	 * @return Bytes Read
	 * @throws IOException
	 */
	public long readPathRecord(OperaPath operaPath, OperaPath parentPath, int tagSize, int recordSize) throws IOException {
		long payloadSize = readPayloadSize(recordSize);

		long read = 0;
		while (read < payloadSize) {
			int tagID = readTagID(tagSize);
			read += tagSize;

			if (tagID == -1) {
				throw new IOException("Unexpected end of file");
			}

			switch (tagID) {
				case O_PATH_NAME:
					OperaStringValue strName = new OperaStringValue();
					read += readStringValue(strName, recordSize);
					String name = strName.getValue();
					operaPath.setName(name);
					if (parentPath != null) {
						operaPath.setFullyQualifiedName(parentPath.getFullyQualifiedName() + "/" + name);
					} else {
						operaPath.setFullyQualifiedName(name);
					}
					break;
				case O_NULL_BYTE:
					break;
				default:
					logger.error("Unexpected Tag ID in Path Record: {}", tagID);
					break;
			}
		}

		if (read != payloadSize) {
			throw new IOException("Read too less or too much bytes. Payload Size: " + payloadSize + ", Read: " + read);
		}
		return read;
	}

	/**
	 * Read cookie record
	 * 
	 * @param operaCookie Cookie
	 * @param tagSize Tag Size
	 * @param recordSize Record Size
	 * @return Bytes Read
	 * @throws IOException
	 */
	public long readCookieRecord(OperaCookie operaCookie, int tagSize, int recordSize) throws IOException {
		long payloadSize = readPayloadSize(recordSize);

		long read = 0;
		while (read < payloadSize) {
			int tagID = readTagID(tagSize);
			read += tagSize;

			if (tagID == -1) {
				throw new IOException("Unexpected end of file");
			}

			switch (tagID) {
				case O_COOKIE_NAME:
					OperaStringValue strName = new OperaStringValue();
					read += readStringValue(strName, recordSize);
					operaCookie.setName(strName.getValue());
					break;
				case O_COOKIE_VALUE:
					OperaStringValue strValue = new OperaStringValue();
					read += readStringValue(strValue, recordSize);
					operaCookie.setValue(strValue.getValue());
					break;
				case O_COOKIE_EXPIRES:
					OperaTimeValue expiresValue = new OperaTimeValue();
					read += readTimeValue(expiresValue, recordSize);
					operaCookie.setExpires(expiresValue.getValue());
					break;
				case O_COOKIE_LAST_USED:
					OperaTimeValue lastUsedValue = new OperaTimeValue();
					read += readTimeValue(lastUsedValue, recordSize);
					operaCookie.setLastUsed(lastUsedValue.getValue());
					break;
				case O_COOKIE_COMMENT:
					OperaStringValue strComment = new OperaStringValue();
					read += readStringValue(strComment, recordSize);
					operaCookie.setComment(strComment.getValue());
					break;
				case O_COOKIE_COMMENT_URL:
					OperaStringValue strCommentURL = new OperaStringValue();
					read += readStringValue(strCommentURL, recordSize);
					operaCookie.setCommentURL(strCommentURL.getValue());
					break;
				case O_COOKIE_RECEIVED_DOMAIN:
					OperaStringValue strReceivedDomain = new OperaStringValue();
					read += readStringValue(strReceivedDomain, recordSize);
					operaCookie.setRecvDomain(strReceivedDomain.getValue());
					break;
				case O_COOKIE_RECEIVED_PATH:
					OperaStringValue strReceivedPath = new OperaStringValue();
					read += readStringValue(strReceivedPath, recordSize);
					operaCookie.setRecvPath(strReceivedPath.getValue());
					break;
				case O_COOKIE_PORTLIST:
					OperaStringValue strPortList = new OperaStringValue();
					read += readStringValue(strPortList, recordSize);
					operaCookie.setPortList(strPortList.getValue());
					break;
				case O_COOKIE_SECURE_1:
				case O_COOKIE_SECURE_2:
				case O_COOKIE_SECURE_3:
				case O_COOKIE_SECURE_4:
					operaCookie.setSecure(true);
					break;
				case O_COOKIE_VERSION:
					OperaByteValue versionValue = new OperaByteValue();
					read += readInt8Value(versionValue, recordSize);
					operaCookie.setVersion(versionValue.getValue());
					break;
				case O_COOKIE_SERVER_ONLY_1:
				case O_COOKIE_SERVER_ONLY_2:
				case O_COOKIE_SERVER_ONLY_3:
				case O_COOKIE_SERVER_ONLY_4:
					operaCookie.setServer(true);
					break;
				case O_COOKIE_PROTECTED_1:
				case O_COOKIE_PROTECTED_2:
				case O_COOKIE_PROTECTED_3:
				case O_COOKIE_PROTECTED_4:
					operaCookie.setDeleteProtected(true);
					break;
				case O_COOKIE_PATH_PREFIX_1:
				case O_COOKIE_PATH_PREFIX_2:
				case O_COOKIE_PATH_PREFIX_3:
				case O_COOKIE_PATH_PREFIX_4:
					operaCookie.setPrefixed(true);
					break;
				case O_COOKIE_PASSWORD_1:
				case O_COOKIE_PASSWORD_2:
				case O_COOKIE_PASSWORD_3:
				case O_COOKIE_PASSWORD_4:
					operaCookie.setPassword(true);
					break;
				case O_COOKIE_AUTHENTICATE_1:
				case O_COOKIE_AUTHENTICATE_2:
				case O_COOKIE_AUTHENTICATE_3:
				case O_COOKIE_AUTHENTICATE_4:
					operaCookie.setAuthenticate(true);
					break;
				case O_COOKIE_THIRD_PARTY_1:
				case O_COOKIE_THIRD_PARTY_2:
				case O_COOKIE_THIRD_PARTY_3:
				case O_COOKIE_THIRD_PARTY_4:
					operaCookie.setThirdParty(true);
					break;
				case O_NULL_BYTE:
					break;
				default:
					logger.error("Unexpected Tag ID in Cookie Record: {}", tagID);
					break;
			}
		}

		if (read != payloadSize) {
			throw new IOException("Read too less or too much bytes. Payload Size: " + payloadSize + ", Read: " + read);
		}
		return read;
	}
}
