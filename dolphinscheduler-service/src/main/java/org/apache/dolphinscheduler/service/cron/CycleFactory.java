/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dolphinscheduler.service.cron;

import com.cronutils.model.Cron;
import com.cronutils.model.field.expression.Always;
import com.cronutils.model.field.expression.QuestionMark;
import org.apache.dolphinscheduler.common.enums.CycleEnum;

/**
 * Cron周期工具工厂，根据Cron表达式创建对应的周期解析器。
 * <p>包含分钟、小时、天、周、月、年六种周期类型的内部实现类。</p>
 */
public class CycleFactory {
    private CycleFactory() {
        throw new IllegalStateException("CycleFactory class");
    }
    /**
     * 创建分钟周期解析器。
     * @param cron Cron对象
     * @return 分钟周期解析器实例
     */
    public static AbstractCycle min(Cron cron) {
      return new MinCycle(cron);
    }

    /**
     * 创建小时周期解析器。
     * @param cron Cron对象
     * @return 小时周期解析器实例
     */
    public static AbstractCycle hour(Cron cron) {
      return new HourCycle(cron);
    }

    /**
     * 创建天周期解析器。
     * @param cron Cron对象
     * @return 天周期解析器实例
     */
    public static AbstractCycle day(Cron cron) {
      return new DayCycle(cron);
    }

    /**
     * 创建周周期解析器。
     * @param cron Cron对象
     * @return 周周期解析器实例
     */
    public static AbstractCycle week(Cron cron) {
      return new WeekCycle(cron);
    }

    /**
     * 创建月周期解析器。
     * @param cron Cron对象
     * @return 月周期解析器实例
     */
    public static AbstractCycle month(Cron cron) {
      return new MonthCycle(cron);
    }

    /**
     * 创建年周期解析器。
     * @param cron Cron对象
     * @return 年周期解析器实例
     */
    public static AbstractCycle year(Cron cron) {
        return new YearCycle(cron);
    }

  /**
   * 天周期解析器，判断Cron表达式是否代表按天调度的任务。
   */
  public static class DayCycle extends AbstractCycle {

    public DayCycle(Cron cron) {
      super(cron);
    }

      /**
       * get cycle
       * @return CycleEnum
       */
        @Override
        protected CycleEnum getCycle() {

          if (minFiledIsSetAll()
              && hourFiledIsSetAll()
              && dayOfMonthFieldIsEvery()
              && dayOfWeekField.getExpression() instanceof QuestionMark
              && monthField.getExpression() instanceof Always) {
            return CycleEnum.DAY;
          }

          return null;
        }

      /**
       * get min cycle
       * @return CycleEnum
       */
        @Override
        protected CycleEnum getMiniCycle() {
          if (dayOfMonthFieldIsEvery()) {
            return CycleEnum.DAY;
          }

          return null;
        }
  }

  /**
   * 小时周期解析器，判断Cron表达式是否代表按小时调度的任务。
   */
  public static class HourCycle extends AbstractCycle {

    public HourCycle(Cron cron) {
      super(cron);
    }

      /**
       * get cycle
       * @return CycleEnum
       */
        @Override
        protected CycleEnum getCycle() {
          if (minFiledIsSetAll()
              && hourFiledIsEvery()
              && dayOfMonthField.getExpression() instanceof Always
              && dayOfWeekField.getExpression() instanceof QuestionMark
              && monthField.getExpression() instanceof Always) {
            return CycleEnum.HOUR;
          }

          return null;
        }

      /**
       * get mini cycle
       * @return CycleEnum
       */
        @Override
        protected CycleEnum getMiniCycle() {
          if(hourFiledIsEvery()){
            return CycleEnum.HOUR;
          }
          return null;
        }
  }

  /**
   * 分钟周期解析器，判断Cron表达式是否代表按分钟调度的任务。
   */
  public static class MinCycle extends AbstractCycle {

      public MinCycle(Cron cron) {
          super(cron);
      }

      /**
       * get cycle
       * @return CycleEnum
       */
      @Override
      protected CycleEnum getCycle() {
          if (minFiledIsEvery()
                  && hourField.getExpression() instanceof Always
                  && dayOfMonthField.getExpression() instanceof Always
                  && monthField.getExpression() instanceof Always) {
              return CycleEnum.MINUTE;
          }

          return null;
      }

      /**
       * get min cycle
       * @return CycleEnum
       */
      @Override
      protected CycleEnum getMiniCycle() {
          if(minFiledIsEvery()){
              return CycleEnum.MINUTE;
          }
          return null;
      }
  }

  /**
   * 月周期解析器，判断Cron表达式是否代表按月调度的任务。
   */
  public static class MonthCycle extends AbstractCycle {

    public MonthCycle(Cron cron) {
      super(cron);
    }

      /**
       * get cycle
       * @return CycleEnum
       */
        @Override
        protected CycleEnum getCycle() {
          boolean flag = (minFiledIsSetAll()
                  && hourFiledIsSetAll()
                  && dayOfMonthFieldIsSetAll()
                  && dayOfWeekField.getExpression() instanceof QuestionMark
                  && monthFieldIsEvery()) ||
                  (minFiledIsSetAll()
                          && hourFiledIsSetAll()
                          && dayOfMonthField.getExpression() instanceof QuestionMark
                          && dayofWeekFieldIsSetAll()
                          && monthFieldIsEvery());
          if (flag) {
            return CycleEnum.MONTH;
          }

          return null;
        }

      /**
       * get mini cycle
       * @return CycleEnum
       */
        @Override
        protected CycleEnum getMiniCycle() {
          if (monthFieldIsEvery()) {
            return CycleEnum.MONTH;
          }

          return null;
        }
  }

  /**
   * 周周期解析器，判断Cron表达式是否代表按周调度的任务。
   */
  public static class WeekCycle extends AbstractCycle {
    public WeekCycle(Cron cron) {
      super(cron);
    }

      /**
       * get cycle
       * @return CycleEnum
       */
        @Override
        protected CycleEnum getCycle() {
          if (minFiledIsSetAll()
              && hourFiledIsSetAll()
              && dayOfMonthField.getExpression() instanceof QuestionMark
              && dayofWeekFieldIsEvery()
              && monthField.getExpression() instanceof Always) {
            return CycleEnum.WEEK;
          }

          return null;
        }

      /**
       * get mini cycle
       * @return CycleEnum
       */
        @Override
        protected CycleEnum getMiniCycle() {
          if (dayofWeekFieldIsEvery()) {
            return CycleEnum.WEEK;
          }

          return null;
        }
  }
    
    /**
     * 年周期解析器，判断Cron表达式是否代表按年调度的任务。
     */
    public static class YearCycle extends AbstractCycle {
        public YearCycle(Cron cron) {
            super(cron);
        }
        
        /**
         * get cycle
         * @return CycleEnum
         */
        @Override
        protected CycleEnum getCycle() {
            boolean flag = (minFiledIsSetAll()
                    && hourFiledIsSetAll()
                    && dayOfMonthFieldIsSetAll()
                    && dayOfWeekField.getExpression() instanceof QuestionMark
                    && monthFieldIsSetAll())
                    && yearFieldIsEvery() ||
                    (minFiledIsSetAll()
                            && hourFiledIsSetAll()
                            && dayOfMonthField.getExpression() instanceof QuestionMark
                            && dayofWeekFieldIsSetAll()
                            && monthFieldIsSetAll()
                            && yearFieldIsEvery());
            
            if (flag) {
                return CycleEnum.YEAR;
            }
            
            return null;
        }
        
        /**
         * get mini cycle
         * @return CycleEnum
         */
        @Override
        protected CycleEnum getMiniCycle() {
            if (yearFieldIsEvery()) {
                return CycleEnum.YEAR;
            }
            
            return null;
        }
    }
}
