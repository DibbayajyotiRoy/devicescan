package com.devicelens.app.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class DeviceDao_Impl implements DeviceDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<DeviceEntity> __insertionAdapterOfDeviceEntity;

  private final EntityDeletionOrUpdateAdapter<DeviceEntity> __updateAdapterOfDeviceEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public DeviceDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDeviceEntity = new EntityInsertionAdapter<DeviceEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `devices` (`id`,`composite_key`,`mac_address`,`device_name`,`vendor`,`detection_method`,`first_seen`,`last_seen`,`seen_count`,`is_trusted_by_user`,`risk_level`,`rssi_last_seen`,`ip_address`,`notes`,`device_type`,`open_ports`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DeviceEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getCompositeKey() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getCompositeKey());
        }
        if (entity.getMacAddress() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getMacAddress());
        }
        if (entity.getDeviceName() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getDeviceName());
        }
        if (entity.getVendor() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getVendor());
        }
        if (entity.getDetectionMethod() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getDetectionMethod());
        }
        statement.bindLong(7, entity.getFirstSeen());
        statement.bindLong(8, entity.getLastSeen());
        statement.bindLong(9, entity.getSeenCount());
        final int _tmp = entity.isTrustedByUser() ? 1 : 0;
        statement.bindLong(10, _tmp);
        if (entity.getRiskLevel() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getRiskLevel());
        }
        if (entity.getRssiLastSeen() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getRssiLastSeen());
        }
        if (entity.getIpAddress() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getIpAddress());
        }
        if (entity.getNotes() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getNotes());
        }
        if (entity.getDeviceType() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getDeviceType());
        }
        if (entity.getOpenPorts() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getOpenPorts());
        }
      }
    };
    this.__updateAdapterOfDeviceEntity = new EntityDeletionOrUpdateAdapter<DeviceEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `devices` SET `id` = ?,`composite_key` = ?,`mac_address` = ?,`device_name` = ?,`vendor` = ?,`detection_method` = ?,`first_seen` = ?,`last_seen` = ?,`seen_count` = ?,`is_trusted_by_user` = ?,`risk_level` = ?,`rssi_last_seen` = ?,`ip_address` = ?,`notes` = ?,`device_type` = ?,`open_ports` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DeviceEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getCompositeKey() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getCompositeKey());
        }
        if (entity.getMacAddress() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getMacAddress());
        }
        if (entity.getDeviceName() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getDeviceName());
        }
        if (entity.getVendor() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getVendor());
        }
        if (entity.getDetectionMethod() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getDetectionMethod());
        }
        statement.bindLong(7, entity.getFirstSeen());
        statement.bindLong(8, entity.getLastSeen());
        statement.bindLong(9, entity.getSeenCount());
        final int _tmp = entity.isTrustedByUser() ? 1 : 0;
        statement.bindLong(10, _tmp);
        if (entity.getRiskLevel() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getRiskLevel());
        }
        if (entity.getRssiLastSeen() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getRssiLastSeen());
        }
        if (entity.getIpAddress() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getIpAddress());
        }
        if (entity.getNotes() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getNotes());
        }
        if (entity.getDeviceType() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getDeviceType());
        }
        if (entity.getOpenPorts() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getOpenPorts());
        }
        statement.bindLong(17, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM devices";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final DeviceEntity device, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfDeviceEntity.insertAndReturnId(device);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final DeviceEntity device, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfDeviceEntity.handle(device);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<DeviceEntity>> observeAll() {
    final String _sql = "SELECT * FROM devices ORDER BY last_seen DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"devices"}, new Callable<List<DeviceEntity>>() {
      @Override
      @NonNull
      public List<DeviceEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCompositeKey = CursorUtil.getColumnIndexOrThrow(_cursor, "composite_key");
          final int _cursorIndexOfMacAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "mac_address");
          final int _cursorIndexOfDeviceName = CursorUtil.getColumnIndexOrThrow(_cursor, "device_name");
          final int _cursorIndexOfVendor = CursorUtil.getColumnIndexOrThrow(_cursor, "vendor");
          final int _cursorIndexOfDetectionMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "detection_method");
          final int _cursorIndexOfFirstSeen = CursorUtil.getColumnIndexOrThrow(_cursor, "first_seen");
          final int _cursorIndexOfLastSeen = CursorUtil.getColumnIndexOrThrow(_cursor, "last_seen");
          final int _cursorIndexOfSeenCount = CursorUtil.getColumnIndexOrThrow(_cursor, "seen_count");
          final int _cursorIndexOfIsTrustedByUser = CursorUtil.getColumnIndexOrThrow(_cursor, "is_trusted_by_user");
          final int _cursorIndexOfRiskLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "risk_level");
          final int _cursorIndexOfRssiLastSeen = CursorUtil.getColumnIndexOrThrow(_cursor, "rssi_last_seen");
          final int _cursorIndexOfIpAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "ip_address");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfDeviceType = CursorUtil.getColumnIndexOrThrow(_cursor, "device_type");
          final int _cursorIndexOfOpenPorts = CursorUtil.getColumnIndexOrThrow(_cursor, "open_ports");
          final List<DeviceEntity> _result = new ArrayList<DeviceEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DeviceEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpCompositeKey;
            if (_cursor.isNull(_cursorIndexOfCompositeKey)) {
              _tmpCompositeKey = null;
            } else {
              _tmpCompositeKey = _cursor.getString(_cursorIndexOfCompositeKey);
            }
            final String _tmpMacAddress;
            if (_cursor.isNull(_cursorIndexOfMacAddress)) {
              _tmpMacAddress = null;
            } else {
              _tmpMacAddress = _cursor.getString(_cursorIndexOfMacAddress);
            }
            final String _tmpDeviceName;
            if (_cursor.isNull(_cursorIndexOfDeviceName)) {
              _tmpDeviceName = null;
            } else {
              _tmpDeviceName = _cursor.getString(_cursorIndexOfDeviceName);
            }
            final String _tmpVendor;
            if (_cursor.isNull(_cursorIndexOfVendor)) {
              _tmpVendor = null;
            } else {
              _tmpVendor = _cursor.getString(_cursorIndexOfVendor);
            }
            final String _tmpDetectionMethod;
            if (_cursor.isNull(_cursorIndexOfDetectionMethod)) {
              _tmpDetectionMethod = null;
            } else {
              _tmpDetectionMethod = _cursor.getString(_cursorIndexOfDetectionMethod);
            }
            final long _tmpFirstSeen;
            _tmpFirstSeen = _cursor.getLong(_cursorIndexOfFirstSeen);
            final long _tmpLastSeen;
            _tmpLastSeen = _cursor.getLong(_cursorIndexOfLastSeen);
            final int _tmpSeenCount;
            _tmpSeenCount = _cursor.getInt(_cursorIndexOfSeenCount);
            final boolean _tmpIsTrustedByUser;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsTrustedByUser);
            _tmpIsTrustedByUser = _tmp != 0;
            final String _tmpRiskLevel;
            if (_cursor.isNull(_cursorIndexOfRiskLevel)) {
              _tmpRiskLevel = null;
            } else {
              _tmpRiskLevel = _cursor.getString(_cursorIndexOfRiskLevel);
            }
            final Integer _tmpRssiLastSeen;
            if (_cursor.isNull(_cursorIndexOfRssiLastSeen)) {
              _tmpRssiLastSeen = null;
            } else {
              _tmpRssiLastSeen = _cursor.getInt(_cursorIndexOfRssiLastSeen);
            }
            final String _tmpIpAddress;
            if (_cursor.isNull(_cursorIndexOfIpAddress)) {
              _tmpIpAddress = null;
            } else {
              _tmpIpAddress = _cursor.getString(_cursorIndexOfIpAddress);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final String _tmpDeviceType;
            if (_cursor.isNull(_cursorIndexOfDeviceType)) {
              _tmpDeviceType = null;
            } else {
              _tmpDeviceType = _cursor.getString(_cursorIndexOfDeviceType);
            }
            final String _tmpOpenPorts;
            if (_cursor.isNull(_cursorIndexOfOpenPorts)) {
              _tmpOpenPorts = null;
            } else {
              _tmpOpenPorts = _cursor.getString(_cursorIndexOfOpenPorts);
            }
            _item = new DeviceEntity(_tmpId,_tmpCompositeKey,_tmpMacAddress,_tmpDeviceName,_tmpVendor,_tmpDetectionMethod,_tmpFirstSeen,_tmpLastSeen,_tmpSeenCount,_tmpIsTrustedByUser,_tmpRiskLevel,_tmpRssiLastSeen,_tmpIpAddress,_tmpNotes,_tmpDeviceType,_tmpOpenPorts);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getAll(final Continuation<? super List<DeviceEntity>> $completion) {
    final String _sql = "SELECT * FROM devices ORDER BY last_seen DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DeviceEntity>>() {
      @Override
      @NonNull
      public List<DeviceEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCompositeKey = CursorUtil.getColumnIndexOrThrow(_cursor, "composite_key");
          final int _cursorIndexOfMacAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "mac_address");
          final int _cursorIndexOfDeviceName = CursorUtil.getColumnIndexOrThrow(_cursor, "device_name");
          final int _cursorIndexOfVendor = CursorUtil.getColumnIndexOrThrow(_cursor, "vendor");
          final int _cursorIndexOfDetectionMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "detection_method");
          final int _cursorIndexOfFirstSeen = CursorUtil.getColumnIndexOrThrow(_cursor, "first_seen");
          final int _cursorIndexOfLastSeen = CursorUtil.getColumnIndexOrThrow(_cursor, "last_seen");
          final int _cursorIndexOfSeenCount = CursorUtil.getColumnIndexOrThrow(_cursor, "seen_count");
          final int _cursorIndexOfIsTrustedByUser = CursorUtil.getColumnIndexOrThrow(_cursor, "is_trusted_by_user");
          final int _cursorIndexOfRiskLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "risk_level");
          final int _cursorIndexOfRssiLastSeen = CursorUtil.getColumnIndexOrThrow(_cursor, "rssi_last_seen");
          final int _cursorIndexOfIpAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "ip_address");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfDeviceType = CursorUtil.getColumnIndexOrThrow(_cursor, "device_type");
          final int _cursorIndexOfOpenPorts = CursorUtil.getColumnIndexOrThrow(_cursor, "open_ports");
          final List<DeviceEntity> _result = new ArrayList<DeviceEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DeviceEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpCompositeKey;
            if (_cursor.isNull(_cursorIndexOfCompositeKey)) {
              _tmpCompositeKey = null;
            } else {
              _tmpCompositeKey = _cursor.getString(_cursorIndexOfCompositeKey);
            }
            final String _tmpMacAddress;
            if (_cursor.isNull(_cursorIndexOfMacAddress)) {
              _tmpMacAddress = null;
            } else {
              _tmpMacAddress = _cursor.getString(_cursorIndexOfMacAddress);
            }
            final String _tmpDeviceName;
            if (_cursor.isNull(_cursorIndexOfDeviceName)) {
              _tmpDeviceName = null;
            } else {
              _tmpDeviceName = _cursor.getString(_cursorIndexOfDeviceName);
            }
            final String _tmpVendor;
            if (_cursor.isNull(_cursorIndexOfVendor)) {
              _tmpVendor = null;
            } else {
              _tmpVendor = _cursor.getString(_cursorIndexOfVendor);
            }
            final String _tmpDetectionMethod;
            if (_cursor.isNull(_cursorIndexOfDetectionMethod)) {
              _tmpDetectionMethod = null;
            } else {
              _tmpDetectionMethod = _cursor.getString(_cursorIndexOfDetectionMethod);
            }
            final long _tmpFirstSeen;
            _tmpFirstSeen = _cursor.getLong(_cursorIndexOfFirstSeen);
            final long _tmpLastSeen;
            _tmpLastSeen = _cursor.getLong(_cursorIndexOfLastSeen);
            final int _tmpSeenCount;
            _tmpSeenCount = _cursor.getInt(_cursorIndexOfSeenCount);
            final boolean _tmpIsTrustedByUser;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsTrustedByUser);
            _tmpIsTrustedByUser = _tmp != 0;
            final String _tmpRiskLevel;
            if (_cursor.isNull(_cursorIndexOfRiskLevel)) {
              _tmpRiskLevel = null;
            } else {
              _tmpRiskLevel = _cursor.getString(_cursorIndexOfRiskLevel);
            }
            final Integer _tmpRssiLastSeen;
            if (_cursor.isNull(_cursorIndexOfRssiLastSeen)) {
              _tmpRssiLastSeen = null;
            } else {
              _tmpRssiLastSeen = _cursor.getInt(_cursorIndexOfRssiLastSeen);
            }
            final String _tmpIpAddress;
            if (_cursor.isNull(_cursorIndexOfIpAddress)) {
              _tmpIpAddress = null;
            } else {
              _tmpIpAddress = _cursor.getString(_cursorIndexOfIpAddress);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final String _tmpDeviceType;
            if (_cursor.isNull(_cursorIndexOfDeviceType)) {
              _tmpDeviceType = null;
            } else {
              _tmpDeviceType = _cursor.getString(_cursorIndexOfDeviceType);
            }
            final String _tmpOpenPorts;
            if (_cursor.isNull(_cursorIndexOfOpenPorts)) {
              _tmpOpenPorts = null;
            } else {
              _tmpOpenPorts = _cursor.getString(_cursorIndexOfOpenPorts);
            }
            _item = new DeviceEntity(_tmpId,_tmpCompositeKey,_tmpMacAddress,_tmpDeviceName,_tmpVendor,_tmpDetectionMethod,_tmpFirstSeen,_tmpLastSeen,_tmpSeenCount,_tmpIsTrustedByUser,_tmpRiskLevel,_tmpRssiLastSeen,_tmpIpAddress,_tmpNotes,_tmpDeviceType,_tmpOpenPorts);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object findByCompositeKey(final String key,
      final Continuation<? super DeviceEntity> $completion) {
    final String _sql = "SELECT * FROM devices WHERE composite_key = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (key == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, key);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<DeviceEntity>() {
      @Override
      @Nullable
      public DeviceEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCompositeKey = CursorUtil.getColumnIndexOrThrow(_cursor, "composite_key");
          final int _cursorIndexOfMacAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "mac_address");
          final int _cursorIndexOfDeviceName = CursorUtil.getColumnIndexOrThrow(_cursor, "device_name");
          final int _cursorIndexOfVendor = CursorUtil.getColumnIndexOrThrow(_cursor, "vendor");
          final int _cursorIndexOfDetectionMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "detection_method");
          final int _cursorIndexOfFirstSeen = CursorUtil.getColumnIndexOrThrow(_cursor, "first_seen");
          final int _cursorIndexOfLastSeen = CursorUtil.getColumnIndexOrThrow(_cursor, "last_seen");
          final int _cursorIndexOfSeenCount = CursorUtil.getColumnIndexOrThrow(_cursor, "seen_count");
          final int _cursorIndexOfIsTrustedByUser = CursorUtil.getColumnIndexOrThrow(_cursor, "is_trusted_by_user");
          final int _cursorIndexOfRiskLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "risk_level");
          final int _cursorIndexOfRssiLastSeen = CursorUtil.getColumnIndexOrThrow(_cursor, "rssi_last_seen");
          final int _cursorIndexOfIpAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "ip_address");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfDeviceType = CursorUtil.getColumnIndexOrThrow(_cursor, "device_type");
          final int _cursorIndexOfOpenPorts = CursorUtil.getColumnIndexOrThrow(_cursor, "open_ports");
          final DeviceEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpCompositeKey;
            if (_cursor.isNull(_cursorIndexOfCompositeKey)) {
              _tmpCompositeKey = null;
            } else {
              _tmpCompositeKey = _cursor.getString(_cursorIndexOfCompositeKey);
            }
            final String _tmpMacAddress;
            if (_cursor.isNull(_cursorIndexOfMacAddress)) {
              _tmpMacAddress = null;
            } else {
              _tmpMacAddress = _cursor.getString(_cursorIndexOfMacAddress);
            }
            final String _tmpDeviceName;
            if (_cursor.isNull(_cursorIndexOfDeviceName)) {
              _tmpDeviceName = null;
            } else {
              _tmpDeviceName = _cursor.getString(_cursorIndexOfDeviceName);
            }
            final String _tmpVendor;
            if (_cursor.isNull(_cursorIndexOfVendor)) {
              _tmpVendor = null;
            } else {
              _tmpVendor = _cursor.getString(_cursorIndexOfVendor);
            }
            final String _tmpDetectionMethod;
            if (_cursor.isNull(_cursorIndexOfDetectionMethod)) {
              _tmpDetectionMethod = null;
            } else {
              _tmpDetectionMethod = _cursor.getString(_cursorIndexOfDetectionMethod);
            }
            final long _tmpFirstSeen;
            _tmpFirstSeen = _cursor.getLong(_cursorIndexOfFirstSeen);
            final long _tmpLastSeen;
            _tmpLastSeen = _cursor.getLong(_cursorIndexOfLastSeen);
            final int _tmpSeenCount;
            _tmpSeenCount = _cursor.getInt(_cursorIndexOfSeenCount);
            final boolean _tmpIsTrustedByUser;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsTrustedByUser);
            _tmpIsTrustedByUser = _tmp != 0;
            final String _tmpRiskLevel;
            if (_cursor.isNull(_cursorIndexOfRiskLevel)) {
              _tmpRiskLevel = null;
            } else {
              _tmpRiskLevel = _cursor.getString(_cursorIndexOfRiskLevel);
            }
            final Integer _tmpRssiLastSeen;
            if (_cursor.isNull(_cursorIndexOfRssiLastSeen)) {
              _tmpRssiLastSeen = null;
            } else {
              _tmpRssiLastSeen = _cursor.getInt(_cursorIndexOfRssiLastSeen);
            }
            final String _tmpIpAddress;
            if (_cursor.isNull(_cursorIndexOfIpAddress)) {
              _tmpIpAddress = null;
            } else {
              _tmpIpAddress = _cursor.getString(_cursorIndexOfIpAddress);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final String _tmpDeviceType;
            if (_cursor.isNull(_cursorIndexOfDeviceType)) {
              _tmpDeviceType = null;
            } else {
              _tmpDeviceType = _cursor.getString(_cursorIndexOfDeviceType);
            }
            final String _tmpOpenPorts;
            if (_cursor.isNull(_cursorIndexOfOpenPorts)) {
              _tmpOpenPorts = null;
            } else {
              _tmpOpenPorts = _cursor.getString(_cursorIndexOfOpenPorts);
            }
            _result = new DeviceEntity(_tmpId,_tmpCompositeKey,_tmpMacAddress,_tmpDeviceName,_tmpVendor,_tmpDetectionMethod,_tmpFirstSeen,_tmpLastSeen,_tmpSeenCount,_tmpIsTrustedByUser,_tmpRiskLevel,_tmpRssiLastSeen,_tmpIpAddress,_tmpNotes,_tmpDeviceType,_tmpOpenPorts);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object findById(final long id, final Continuation<? super DeviceEntity> $completion) {
    final String _sql = "SELECT * FROM devices WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<DeviceEntity>() {
      @Override
      @Nullable
      public DeviceEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCompositeKey = CursorUtil.getColumnIndexOrThrow(_cursor, "composite_key");
          final int _cursorIndexOfMacAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "mac_address");
          final int _cursorIndexOfDeviceName = CursorUtil.getColumnIndexOrThrow(_cursor, "device_name");
          final int _cursorIndexOfVendor = CursorUtil.getColumnIndexOrThrow(_cursor, "vendor");
          final int _cursorIndexOfDetectionMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "detection_method");
          final int _cursorIndexOfFirstSeen = CursorUtil.getColumnIndexOrThrow(_cursor, "first_seen");
          final int _cursorIndexOfLastSeen = CursorUtil.getColumnIndexOrThrow(_cursor, "last_seen");
          final int _cursorIndexOfSeenCount = CursorUtil.getColumnIndexOrThrow(_cursor, "seen_count");
          final int _cursorIndexOfIsTrustedByUser = CursorUtil.getColumnIndexOrThrow(_cursor, "is_trusted_by_user");
          final int _cursorIndexOfRiskLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "risk_level");
          final int _cursorIndexOfRssiLastSeen = CursorUtil.getColumnIndexOrThrow(_cursor, "rssi_last_seen");
          final int _cursorIndexOfIpAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "ip_address");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfDeviceType = CursorUtil.getColumnIndexOrThrow(_cursor, "device_type");
          final int _cursorIndexOfOpenPorts = CursorUtil.getColumnIndexOrThrow(_cursor, "open_ports");
          final DeviceEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpCompositeKey;
            if (_cursor.isNull(_cursorIndexOfCompositeKey)) {
              _tmpCompositeKey = null;
            } else {
              _tmpCompositeKey = _cursor.getString(_cursorIndexOfCompositeKey);
            }
            final String _tmpMacAddress;
            if (_cursor.isNull(_cursorIndexOfMacAddress)) {
              _tmpMacAddress = null;
            } else {
              _tmpMacAddress = _cursor.getString(_cursorIndexOfMacAddress);
            }
            final String _tmpDeviceName;
            if (_cursor.isNull(_cursorIndexOfDeviceName)) {
              _tmpDeviceName = null;
            } else {
              _tmpDeviceName = _cursor.getString(_cursorIndexOfDeviceName);
            }
            final String _tmpVendor;
            if (_cursor.isNull(_cursorIndexOfVendor)) {
              _tmpVendor = null;
            } else {
              _tmpVendor = _cursor.getString(_cursorIndexOfVendor);
            }
            final String _tmpDetectionMethod;
            if (_cursor.isNull(_cursorIndexOfDetectionMethod)) {
              _tmpDetectionMethod = null;
            } else {
              _tmpDetectionMethod = _cursor.getString(_cursorIndexOfDetectionMethod);
            }
            final long _tmpFirstSeen;
            _tmpFirstSeen = _cursor.getLong(_cursorIndexOfFirstSeen);
            final long _tmpLastSeen;
            _tmpLastSeen = _cursor.getLong(_cursorIndexOfLastSeen);
            final int _tmpSeenCount;
            _tmpSeenCount = _cursor.getInt(_cursorIndexOfSeenCount);
            final boolean _tmpIsTrustedByUser;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsTrustedByUser);
            _tmpIsTrustedByUser = _tmp != 0;
            final String _tmpRiskLevel;
            if (_cursor.isNull(_cursorIndexOfRiskLevel)) {
              _tmpRiskLevel = null;
            } else {
              _tmpRiskLevel = _cursor.getString(_cursorIndexOfRiskLevel);
            }
            final Integer _tmpRssiLastSeen;
            if (_cursor.isNull(_cursorIndexOfRssiLastSeen)) {
              _tmpRssiLastSeen = null;
            } else {
              _tmpRssiLastSeen = _cursor.getInt(_cursorIndexOfRssiLastSeen);
            }
            final String _tmpIpAddress;
            if (_cursor.isNull(_cursorIndexOfIpAddress)) {
              _tmpIpAddress = null;
            } else {
              _tmpIpAddress = _cursor.getString(_cursorIndexOfIpAddress);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final String _tmpDeviceType;
            if (_cursor.isNull(_cursorIndexOfDeviceType)) {
              _tmpDeviceType = null;
            } else {
              _tmpDeviceType = _cursor.getString(_cursorIndexOfDeviceType);
            }
            final String _tmpOpenPorts;
            if (_cursor.isNull(_cursorIndexOfOpenPorts)) {
              _tmpOpenPorts = null;
            } else {
              _tmpOpenPorts = _cursor.getString(_cursorIndexOfOpenPorts);
            }
            _result = new DeviceEntity(_tmpId,_tmpCompositeKey,_tmpMacAddress,_tmpDeviceName,_tmpVendor,_tmpDetectionMethod,_tmpFirstSeen,_tmpLastSeen,_tmpSeenCount,_tmpIsTrustedByUser,_tmpRiskLevel,_tmpRssiLastSeen,_tmpIpAddress,_tmpNotes,_tmpDeviceType,_tmpOpenPorts);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object countByRiskLevel(final String level,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM devices WHERE risk_level = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (level == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, level);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
