package org.artoolkit.ar.ARPokemonBattle.Util;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * Data sent via bluetooth use a single byte "op_code" value followed by the payload
 *
 * Pokemon selected:
 *   Payload:   1 byte, Pokemon index in POKEMON_SELECT_LIST
 *              1 byte, Ready status
 *
 * Move selected:
 *   Payload:   1 byte, move index in move list
 *
 * Move list:
 *   Payload:   1 byte, move index in move list
 *              Parceled Move
 *
 * Move list Ack:
 *   Payload:   1 byte, move index received
 *
 * Pokemon name:
 *   Payload:   1 byte, 0x1 for name of host or 0x2 for name of client
 *              1 byte, length of String payload
 *              String, name of Pokemon
 *
 * Pokemon level:
 *   Payload:   1 byte, 0x1 for level of host or 0x2 for level of client
 *              1 byte, level of Pokemon
 *
 * Pokemon HP:
 *   Payload:   1 byte, 0x1 for current client HP or 0x2 for client max HP
 *              1 byte, HP value
 *
 * Display data ack:
 *   Payload:   "Don't care", used to notify host that client is ready to receive again
 *
 * Ready for data:
 *   Payload:   "Don't care", used to notify host that client is ready for data
 *
 * Ready for data ack:
 *   Payload:   "Don't care", used to notify client that host received the data request
 *
 * Handshake request:
 *   Payload:   "Don't care", used to notify host that client is ready with BT service
 *
 * Handshake ack:
 *   Payload:   "Don't care", used to confirm that host received handshake request
 *
 *
 * Battle sim step:
 *   Payload:   1 byte, index of battle sim step in host's queue
 *              1 byte, size of host's queue
 *              1 byte, length of simulation step packet
 *              Byte[], packet of battle simulation step, which will contain in entirety one
 *                      of the three types below
 *
 * Battle message:
 *   Payload:   1 byte, length of String payload
 *              String, display message
 *
 * Animate health:
 *   Payload:   1 byte, 0x1 for host HP or 0x2 for client HP
 *              4 bytes, long value for current percentage
 *              4 bytes, long value for final percentage
 *              1 byte, damage (IFF ANIMATE_CLIENT_HEALTH)
 *
 * Fainted:
 *   Payload:   1 byte, 0x1 for host fainted or 0x2 for client fainted
 *
 * Battle sim step ack:
 *   Payload:   1 byte, index of battle sim step received
 *
 * Bag item:
 *   Payload:   1 byte, item ID (defined in GameDefinitions)
 *              1 byte, item quantity to add
 *
 * Bag item ack:
 *   Payload:   Don't care
 *
 * Item selected:
 *   Payload:   1 byte, item ID selected by client
 *
 * Exhausted:
 *   Payload:   1 byte, 0x0 for both exhausted, 0x1 for host, or 0x2 for client
 */
public final class BluetoothPacketHelper {
    public static final int OP_CODE_POKEMON_SELECT = 0x1;
    public static final int OP_CODE_MOVE_SELECT = 0x2;
    public static final int OP_CODE_MOVE_LIST = 0x3;
    public static final int OP_CODE_MOVE_LIST_ACK = 0x4;
    public static final int OP_CODE_READY_FOR_DATA = 0x9;
    public static final int OP_CODE_BAG_ITEM = 0x10;
    public static final int OP_CODE_BAG_ITEM_ACK = 0x11;
    public static final int OP_CODE_ITEM_SELECT = 0x12;

    /** Handshake at start of BattleActivity **/
    public static final int OP_CODE_HANDSHAKE_RQ = 0x13;
    public static final int OP_CODE_HANDSHAKE_ACK = 0x14;

    /**
     * Battle simulation messages
     */
    public static final int OP_CODE_BATTLE_SIM_STEP = 0xA;
    public static final int OP_CODE_BATTLE_SIM_STEP_ACK = 0xB;
    public static final int OP_CODE_BATTLE_MESSAGE = 0xC;
    public static final int OP_CODE_ANIMATE_HEALTH = 0xD;
    public static final int OP_CODE_POKEMON_FAINTED = 0xE;
    public static final int OP_CODE_POKEMON_EXHAUSTED = 0xF;

    // If these values change, make sure to check the Host sending values in code
    public static final int OP_CODE_POKEMON_NAME = 0x5;
    public static final int OP_CODE_POKEMON_HP = 0x6;
    public static final int OP_CODE_POKEMON_LEVEL = 0x7;
    public static final int OP_CODE_DISPLAY_DATA_ACK = 0x8;

    /** Parameter target values for op codes **/
    public static final int TARGET_BOTH = 0x0;
    public static final int TARGET_HOST = 0x1;
    public static final int TARGET_CLIENT = 0x2;

    /** Values for OP_CODE_POKEMON_HP **/
    public static final int POKEMON_HP_CURRENT = 0x1;
    public static final int POKEMON_HP_MAX = 0x2;

    public static byte[] createPacket(int opcode, byte[] payload) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        out.write(opcode);
        // Assumes 1 byte for op code
        out.write(payload, 0, payload.length);

        return out.toByteArray();
    }

    public static byte[] createPacket(int opcode, int[] values) {
        byte[] packet = new byte[values.length + 1];
        int index = 0;

        packet[index++] = Integer.valueOf(opcode).byteValue();
        for (int i = 0; i < values.length; i++, index++) {
            packet[index] = Integer.valueOf(values[i]).byteValue();
        }

        return packet;
    }

    public static byte[] createPacket(int opcode, int value) {
        byte[] packet = new byte[2];

        packet[0] = Integer.valueOf(opcode).byteValue();
        packet[1] = Integer.valueOf(value).byteValue();

        return packet;
    }

    public static byte[] appendByteArrays(byte[] first, byte[] second) {
        byte[] combined = new byte[first.length + second.length];

        System.arraycopy(first, 0, combined, 0, first.length);
        System.arraycopy(second, 0, combined, first.length, second.length);

        return combined;
    }

    public static byte[] appendByteArrayToInt(int val, byte[] array) {
        byte[] appended = new byte[array.length + 1];

        appended[0] = Integer.valueOf(val).byteValue();
        System.arraycopy(array, 0, appended, 1, array.length);

        return appended;
    }

    public static float convertToFloat(byte[] b) {
        return ByteBuffer.wrap(b).getFloat();
    }

    public static byte[] convertToByteArray(float f) {
        return ByteBuffer.allocate(4).putFloat(f).array();
    }
}
