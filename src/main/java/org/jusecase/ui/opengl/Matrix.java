package org.jusecase.ui.opengl;

import org.jusecase.scenegraph.math.Matrix3x2;

public class Matrix {
    private static final double[] TEMP_MATRIX_4X4 = new double[16];
    private static final float[] TEMP_MATRIX_4X4f = new float[16];

    public static double[] toOpenGlMatrix4d(Matrix3x2 matrix) {

        TEMP_MATRIX_4X4[0] = matrix.a;
        TEMP_MATRIX_4X4[1] = matrix.c;
        TEMP_MATRIX_4X4[2] = 0;
        TEMP_MATRIX_4X4[3] = 0;

        TEMP_MATRIX_4X4[4] = matrix.b;
        TEMP_MATRIX_4X4[5] = matrix.d;
        TEMP_MATRIX_4X4[6] = 0;
        TEMP_MATRIX_4X4[7] = 0;

        TEMP_MATRIX_4X4[8] = 0;
        TEMP_MATRIX_4X4[9] = 0;
        TEMP_MATRIX_4X4[10] = 1;
        TEMP_MATRIX_4X4[11] = 0;

        TEMP_MATRIX_4X4[12] = matrix.tx;
        TEMP_MATRIX_4X4[13] = matrix.ty;
        TEMP_MATRIX_4X4[14] = 0;
        TEMP_MATRIX_4X4[15] = 1;

        return TEMP_MATRIX_4X4;
    }

    public static float[] toOpenGlMatrix4f(Matrix3x2 matrix) {

        TEMP_MATRIX_4X4f[0] = (float)matrix.a;
        TEMP_MATRIX_4X4f[1] = (float)matrix.c;
        TEMP_MATRIX_4X4f[2] = 0;
        TEMP_MATRIX_4X4f[3] = 0;

        TEMP_MATRIX_4X4f[4] = (float)matrix.b;
        TEMP_MATRIX_4X4f[5] = (float)matrix.d;
        TEMP_MATRIX_4X4f[6] = 0;
        TEMP_MATRIX_4X4f[7] = 0;

        TEMP_MATRIX_4X4f[8] = 0;
        TEMP_MATRIX_4X4f[9] = 0;
        TEMP_MATRIX_4X4f[10] = 1;
        TEMP_MATRIX_4X4f[11] = 0;

        TEMP_MATRIX_4X4f[12] = (float)matrix.tx;
        TEMP_MATRIX_4X4f[13] = (float)matrix.ty;
        TEMP_MATRIX_4X4f[14] = 0;
        TEMP_MATRIX_4X4f[15] = 1;

        return TEMP_MATRIX_4X4f;
    }
}
