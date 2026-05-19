class ReportGenerator:

    @staticmethod
    def generate(metrics):

        print("\n===== ATTACK REPORT =====")

        print(f"Total Requests : {metrics.total}")
        print(f"Success        : {metrics.success}")
        print(f"Failed         : {metrics.failed}")
        print(f"Locked         : {metrics.locked}")
        print(f"Rate Limited   : {metrics.rate_limited}")